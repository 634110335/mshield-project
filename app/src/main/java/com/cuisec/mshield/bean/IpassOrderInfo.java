package com.cuisec.mshield.bean;

import java.util.List;

public class IpassOrderInfo {

    /**
     * code : string
     * data : [{"acceptid":"string","acceptno":"string","addTime":"string","businesstype":"string","invoiceType":"string","ipassno":"string","payaudit":"string","paymoney":0,"paystatus":"string","paystyle":"string","status":"string"}]
     * hasSuccess : true
     * msg : string
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
         * acceptid : string
         * acceptno : string
         * addTime : string
         * businesstype : string
         * invoiceType : string
         * ipassno : string
         * payaudit : string
         * paymoney : 0
         * paystatus : string
         * paystyle : string
         * status : string
         */

        private String acceptid;
        private String acceptno;
        private String addTime;
        private String businesstype;
        private String invoiceType;
        private String ipassno;
        private String payaudit;
        private int paymoney;
        private String paystatus;
        private String paystyle;
        private String status;

        public String getAcceptid() {
            return acceptid;
        }

        public void setAcceptid(String acceptid) {
            this.acceptid = acceptid;
        }

        public String getAcceptno() {
            return acceptno;
        }

        public void setAcceptno(String acceptno) {
            this.acceptno = acceptno;
        }

        public String getAddTime() {
            return addTime;
        }

        public void setAddTime(String addTime) {
            this.addTime = addTime;
        }

        public String getBusinesstype() {
            return businesstype;
        }

        public void setBusinesstype(String businesstype) {
            this.businesstype = businesstype;
        }

        public String getInvoiceType() {
            return invoiceType;
        }

        public void setInvoiceType(String invoiceType) {
            this.invoiceType = invoiceType;
        }

        public String getIpassno() {
            return ipassno;
        }

        public void setIpassno(String ipassno) {
            this.ipassno = ipassno;
        }

        public String getPayaudit() {
            return payaudit;
        }

        public void setPayaudit(String payaudit) {
            this.payaudit = payaudit;
        }

        public int getPaymoney() {
            return paymoney;
        }

        public void setPaymoney(int paymoney) {
            this.paymoney = paymoney;
        }

        public String getPaystatus() {
            return paystatus;
        }

        public void setPaystatus(String paystatus) {
            this.paystatus = paystatus;
        }

        public String getPaystyle() {
            return paystyle;
        }

        public void setPaystyle(String paystyle) {
            this.paystyle = paystyle;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
