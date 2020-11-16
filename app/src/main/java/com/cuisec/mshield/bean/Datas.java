package com.cuisec.mshield.bean;

import java.util.List;

public class Datas {

    /**
     * code : 200
     * data : [{"acceptId":"B202007031029486173","childData":[{"logId":"ff80808173127f7c01731282cbf10004","opDate":"2020-07-03 10:30:28","opDesc":"申请已提交","postCode":""},{"logId":"ff8080817356c08101735be7c9c80006","opDate":"2020-07-17 16:33:03","opDesc":"121212--管理员作废","postCode":""},{"logId":"ff808081738ef84b01738f04cccb0007","opDate":"2020-07-27 14:45:23","opDesc":"订单已支付","postCode":""},{"logId":"ff808081738ef84b01738f04cd920008","opDate":"2020-07-27 14:45:23","opDesc":"订单已审核","postCode":""},{"logId":"ff80808173947e46017398294dbc0002","opDate":"2020-07-29 09:19:43","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"},{"logId":"ff80808173947e4601739829e9b90004","opDate":"2020-07-29 09:20:23","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"},{"logId":"ff80808173947e460173982aadac0006","opDate":"2020-07-29 09:21:13","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"}]}]
     * hasSuccess : true
     * msg : 操作成功
     */

    private String code;
    private boolean hasSuccess;
    private String msg;
    private List<DataBean> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isHasSuccess() {
        return hasSuccess;
    }

    public void setHasSuccess(boolean hasSuccess) {
        this.hasSuccess = hasSuccess;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * acceptId : B202007031029486173
         * childData : [{"logId":"ff80808173127f7c01731282cbf10004","opDate":"2020-07-03 10:30:28","opDesc":"申请已提交","postCode":""},{"logId":"ff8080817356c08101735be7c9c80006","opDate":"2020-07-17 16:33:03","opDesc":"121212--管理员作废","postCode":""},{"logId":"ff808081738ef84b01738f04cccb0007","opDate":"2020-07-27 14:45:23","opDesc":"订单已支付","postCode":""},{"logId":"ff808081738ef84b01738f04cd920008","opDate":"2020-07-27 14:45:23","opDesc":"订单已审核","postCode":""},{"logId":"ff80808173947e46017398294dbc0002","opDate":"2020-07-29 09:19:43","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"},{"logId":"ff80808173947e4601739829e9b90004","opDate":"2020-07-29 09:20:23","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"},{"logId":"ff80808173947e460173982aadac0006","opDate":"2020-07-29 09:21:13","opDesc":"iPASS已寄出--操作员：王威","postCode":"111111111111111111111111"}]
         */

        private String acceptId;
        private List<ChildDataBean> childData;

        public String getAcceptId() {
            return acceptId;
        }

        public void setAcceptId(String acceptId) {
            this.acceptId = acceptId;
        }

        public List<ChildDataBean> getChildData() {
            return childData;
        }

        public void setChildData(List<ChildDataBean> childData) {
            this.childData = childData;
        }

        public static class ChildDataBean {
            /**
             * logId : ff80808173127f7c01731282cbf10004
             * opDate : 2020-07-03 10:30:28
             * opDesc : 申请已提交
             * postCode :
             */

            private String logId;
            private String opDate;
            private String opDesc;
            private String postCode;

            public String getLogId() {
                return logId;
            }

            public void setLogId(String logId) {
                this.logId = logId;
            }

            public String getOpDate() {
                return opDate;
            }

            public void setOpDate(String opDate) {
                this.opDate = opDate;
            }

            public String getOpDesc() {
                return opDesc;
            }

            public void setOpDesc(String opDesc) {
                this.opDesc = opDesc;
            }

            public String getPostCode() {
                return postCode;
            }

            public void setPostCode(String postCode) {
                this.postCode = postCode;
            }
        }
    }
}
