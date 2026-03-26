package dsa1.xczxx.wfs.ws.plugin.form;


import kd.bos.algo.DataSet;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.fi.fa.business.utils.FaBillParamUtils;
import kd.fi.fa.business.utils.FaPermissionUtils;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.common.util.Fa;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

public class CashierHomepage extends AbstractBillPlugIn {

    static Log logger = LogFactory.getLog(CashierHomepage.class);

    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
        if ("dsa1_orgid".equalsIgnoreCase(name)) {
            Object newValue = e.getChangeSet()[0].getNewValue();
            if (newValue == null) {
                this.getModel().setValue("orgid", e.getChangeSet()[0].getOldValue());
                return;
            }
            DynamicObject org = (DynamicObject)newValue;
            Object orgid = org.getPkValue();
            this.showMainBookInfo((Long)orgid);
            this.getView().updateView("dsa1_curperiodid");
        }



    }
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        try {
       List<Long> orgs =  UserServiceHelper.getUserChangeAbleOrg(ContextUtil.getUserId(), true).getHasPermOrgs();

            List<Long> orgList = FaPermissionUtils.getPermissionLeafOrgIdsV2(this.getView().getPageId(), this.getView().getEntityId(), "47150e89000000ac", "10");
            if (orgList.size() == 0) {
                this.getView().setEnable(false, new String[]{"orgid"});
                this.getModel().setValue("inited", true);
            } else {
                this.getPageCache().put("orgF7PageCache", Fa.join(orgList, ","));
                Long org = FaPermissionUtils.getDefaultAcctOrg(orgList);
                IDataModel model = this.getModel();
                model.setValue("dsa1_orgid", org);
                this.showMainBookInfo(org);
                boolean showInitPage = FaBillParamUtils.getBooleanValue((Long) null, (Long) null, (String) null, "fa_mainpage_init_param");
                this.getPageCache().put("showInitPage", String.valueOf(showInitPage));
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

    }


    private void showMainBookInfo(long orgid) {
        IDataModel model = this.getModel();
        DynamicObject mainBook = getAsstBookByOrg(orgid);
        if (mainBook != null) {
            this.getView().setVisible(true, new String[]{"dsa1_curperiodid"});
            model.setValue("dsa1_curperiodid", mainBook.getLong("curperiod"));
        } else {
            this.getView().setVisible(false, new String[]{"dsa1_curperiodid"});
        }

        this.getView().updateView("dsa1_curperiodid");
    }

    private DynamicObject getAsstBookByOrg(long orgid) {
        QFilter checkStatusFilter = new QFilter("checkoutstatus", "in", Arrays.asList("1", "2", "4"));
        QFilter orgQfilter = new QFilter("org","=",orgid);
        DynamicObjectCollection curperiodCollection = QueryServiceHelper.query( "cas_finalcheckout", "id booktype,org,period as curperiod", new QFilter[]{orgQfilter, checkStatusFilter});
        DynamicObject dynamicObject= null;
        if(curperiodCollection.size()==1){
            dynamicObject = curperiodCollection.get(0);
        }


        return dynamicObject;
    }


}
