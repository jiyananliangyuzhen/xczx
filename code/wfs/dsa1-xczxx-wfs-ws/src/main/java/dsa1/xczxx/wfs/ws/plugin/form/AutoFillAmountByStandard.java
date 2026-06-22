package dsa1.xczxx.wfs.ws.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 招待费申请单 - 根据招待类型/招待方式/接待级别自动带出招待金额
 * 映射表：dsa1_zdf004（费用标准）
 */
public class AutoFillAmountByStandard extends AbstractBillPlugIn {
    private static final Log logger = LogFactory.getLog(AutoFillAmountByStandard.class);

    // 明细表实体名
    private static final String EXPENSE_ENTRY = "dsa1_expenseentryentity";
    // 映射表实体名
    private static final String STANDARD_ENTITY = "dsa1_zdf004";

    // 明细表字段
    private static final String FIELD_HOSPITALITY_TYPE = "dsa1_hospitalitytype";   // 招待类型
    private static final String FIELD_HOSPITALITY_WAY = "dsa1_hospitalityway";     // 招待方式
    private static final String FIELD_RECEPTION_LEVEL = "dsa1_receptionlevel";     // 接待级别
    private static final String FIELD_AMOUNT = "dsa1_amount";                       // 招待金额

    // 映射表字段
    private static final String STANDARD_HOSPITALITY_TYPE = "dsa1_hospitalitytype";
    private static final String STANDARD_HOSPITALITY_WAY = "dsa1_hospitalityway";
    private static final String STANDARD_RECEPTION_LEVEL = "dsa1_receptionlevel";
    private static final String STANDARD_AMOUNT = "dsa1_amount";

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);

        String fieldName = e.getProperty().getName();

        // 只关心三个联动字段
        if (!FIELD_HOSPITALITY_TYPE.equals(fieldName)
                && !FIELD_HOSPITALITY_WAY.equals(fieldName)
                && !FIELD_RECEPTION_LEVEL.equals(fieldName)) {
            return;
        }
        String uploadFilName = "20aeab2d46803000";
        FileService attachmentFileService = FileServiceFactory.getAttachmentFileService();
        String realPath = attachmentFileService.getFileServiceExt().getRealPath(uploadFilName);
        System.out.println(realPath);



        // 获取当前行索引
        int currentRowIndex = this.getModel().getEntryCurrentRowIndex(EXPENSE_ENTRY);
        if (currentRowIndex < 0) {
            return;
        }
        IDataModel model = this.getModel();
        // 获取当前行的三个字段值
        DynamicObject typeObj = (DynamicObject) model.getValue(FIELD_HOSPITALITY_TYPE, currentRowIndex);
        DynamicObject wayObj = (DynamicObject) model.getValue(FIELD_HOSPITALITY_WAY, currentRowIndex);
        DynamicObject levelObj = (DynamicObject) model.getValue(FIELD_RECEPTION_LEVEL, currentRowIndex);

        // 三个字段都必须有值，才进行查询
        if (typeObj == null || wayObj == null || levelObj == null) {
            logger.debug("招待费标准字段不完整，招待类型/招待方式/接待级别任一为空，不查询金额");
            return;
        }

        // 获取三个字段的ID（假设是基础资料类型，需要取id进行匹配）
        Long typeId = typeObj.getLong("id");
        Long wayId = wayObj.getLong("id");
        Long levelId = levelObj.getLong("id");

        if (typeId == null || wayId == null || levelId == null) {
            logger.debug("招待费标准字段ID为空，不查询金额");
            return;
        }

        // 根据三个条件查询映射表，获取金额
        BigDecimal amount = queryAmountFromStandard(typeId, wayId, levelId);

        if (amount != null) {
            model.setValue(FIELD_AMOUNT, amount, currentRowIndex);
            logger.info("自动带出招待金额: {}, 条件: 招待类型ID={}, 招待方式ID={}, 接待级别ID={}",
                    amount, typeId, wayId, levelId);
        } else {
            // 可选：如果查询不到，可以清空金额或提示用户
            // model.setValue(FIELD_AMOUNT, null, currentRowIndex);
            logger.warn("未找到匹配的招待费用标准，招待类型ID={}, 招待方式ID={}, 接待级别ID={}",
                    typeId, wayId, levelId);
        }
    }

    /**
     * 根据招待类型、招待方式、接待级别查询映射表，获取招待金额
     * @param hospitalityTypeId 招待类型ID
     * @param hospitalityWayId 招待方式ID
     * @param receptionLevelId 接待级别ID
     * @return 招待金额，未找到返回null
     */
    private BigDecimal queryAmountFromStandard(Long hospitalityTypeId, Long hospitalityWayId, Long receptionLevelId) {
        try {
            // 构建过滤条件
            List<QFilter> filters = new ArrayList<>();
            filters.add(new QFilter(STANDARD_HOSPITALITY_TYPE, QCP.equals, hospitalityTypeId));
            filters.add(new QFilter(STANDARD_HOSPITALITY_WAY, QCP.equals, hospitalityWayId));
            filters.add(new QFilter(STANDARD_RECEPTION_LEVEL, QCP.equals, receptionLevelId));

            // 查询字段列表
            String selectFields = STANDARD_AMOUNT;

            // 执行查询
            DynamicObjectCollection standards = QueryServiceHelper.query(STANDARD_ENTITY,
                    selectFields, filters.toArray(new QFilter[0]));

            if (standards != null && standards.size() > 0) {
                // 取第一条匹配记录
                return standards.get(0).getBigDecimal(STANDARD_AMOUNT);
            }
        } catch (Exception e) {
            logger.error("查询费用标准映射表失败", e);
        }
        return null;
    }
}
