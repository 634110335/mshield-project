package com.cuisec.mshield.bean;

import java.util.List;

public class IpassDetailsInfo {


    /**
     * hasSuccess : true
     * code : 200
     * msg : 操作成功
     * data : [{"acceptId":"B202007031034031384","childData":[{"logId":"ff80808173127f7c01731287453f000a","opDesc":"申请已提交","postCode":"","opDate":"2020-07-03 10:35:21"},{"logId":"ff8080817356c08101735bc84c5d0003","opDesc":"订单已支付","postCode":"","opDate":"2020-07-17 15:58:40"},{"logId":"ff8080817356c08101735bc84ca50004","opDesc":"订单已审核","postCode":"","opDate":"2020-07-17 15:58:40"}]}]
     */

    private boolean hasSuccess;
    private String code;
    private String msg;
    private List<DataBean> data;

    public boolean isHasSuccess() {
        return hasSuccess;
    }

    public void setHasSuccess(boolean hasSuccess) {
        this.hasSuccess = hasSuccess;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
         * acceptId : B202007031034031384
         * childData : [{"logId":"ff80808173127f7c01731287453f000a","opDesc":"申请已提交","postCode":"","opDate":"2020-07-03 10:35:21"},{"logId":"ff8080817356c08101735bc84c5d0003","opDesc":"订单已支付","postCode":"","opDate":"2020-07-17 15:58:40"},{"logId":"ff8080817356c08101735bc84ca50004","opDesc":"订单已审核","postCode":"","opDate":"2020-07-17 15:58:40"}]
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
             * logId : ff80808173127f7c01731287453f000a
             * opDesc : 申请已提交
             * postCode :
             * opDate : 2020-07-03 10:35:21
             */

            private String logId;
            private String opDesc;
            private String postCode;
            private String opDate;

            public String getLogId() {
                return logId;
            }

            public void setLogId(String logId) {
                this.logId = logId;
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

            public String getOpDate() {
                return opDate;
            }

            public void setOpDate(String opDate) {
                this.opDate = opDate;
            }
        }
    }
}
