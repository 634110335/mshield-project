package com.cuisec.mshield.bean;

import java.util.List;

public class ProvinceDataBean {

    /**
     * hasSuccess : true
     * code : 200
     * msg : 操作成功
     * data : [{"sid":34,"provinceId":"820000","province":"澳门特别行政区"},{"sid":33,"provinceId":"810000","province":"香港特别行政区"},{"sid":32,"provinceId":"710000","province":"台湾省"},{"sid":31,"provinceId":"650000","province":"新疆维吾尔自治区"},{"sid":30,"provinceId":"640000","province":"宁夏回族自治区"},{"sid":29,"provinceId":"630000","province":"青海省"},{"sid":28,"provinceId":"620000","province":"甘肃省"},{"sid":27,"provinceId":"610000","province":"陕西省"},{"sid":26,"provinceId":"540000","province":"西藏自治区"},{"sid":25,"provinceId":"530000","province":"云南省"},{"sid":24,"provinceId":"520000","province":"贵州省"},{"sid":23,"provinceId":"510000","province":"四川省"},{"sid":22,"provinceId":"500000","province":"重庆市"},{"sid":21,"provinceId":"460000","province":"海南省"},{"sid":20,"provinceId":"450000","province":"广西壮族自治区"},{"sid":19,"provinceId":"440000","province":"广东省"},{"sid":18,"provinceId":"430000","province":"湖南省"},{"sid":17,"provinceId":"420000","province":"湖北省"},{"sid":16,"provinceId":"410000","province":"河南省"},{"sid":15,"provinceId":"370000","province":"山东省"},{"sid":14,"provinceId":"360000","province":"江西省"},{"sid":13,"provinceId":"350000","province":"福建省"},{"sid":12,"provinceId":"340000","province":"安徽省"},{"sid":11,"provinceId":"330000","province":"浙江省"},{"sid":10,"provinceId":"320000","province":"江苏省"},{"sid":9,"provinceId":"310000","province":"上海市"},{"sid":8,"provinceId":"230000","province":"黑龙江省"},{"sid":7,"provinceId":"220000","province":"吉林省"},{"sid":6,"provinceId":"210000","province":"辽宁省"},{"sid":5,"provinceId":"150000","province":"内蒙古自治区"},{"sid":4,"provinceId":"140000","province":"山西省"},{"sid":3,"provinceId":"130000","province":"河北省"},{"sid":2,"provinceId":"120000","province":"天津市"},{"sid":1,"provinceId":"110000","province":"北京市"}]
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
         * sid : 34
         * provinceId : 820000
         * province : 澳门特别行政区
         */

        private int sid;
        private String provinceId;
        private String province;

        public int getSid() {
            return sid;
        }

        public void setSid(int sid) {
            this.sid = sid;
        }

        public String getProvinceId() {
            return provinceId;
        }

        public void setProvinceId(String provinceId) {
            this.provinceId = provinceId;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }
    }
}
