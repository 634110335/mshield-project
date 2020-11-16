package com.cuisec.mshield.bean;

/**
 * @author dy
 * @date 2019/11/6
 * @function
 */
public class SetSealBean {

    /**
     * ret : 0
     * msg : success
     * data : {"seal":true}
     */

    private int ret;
    private String msg;
    private DataBean data;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * seal : true
         */

        private boolean seal;

        public boolean getSeal() {
            return seal;
        }

        public void setSeal(boolean seal) {
            this.seal = seal;
        }
    }
}
