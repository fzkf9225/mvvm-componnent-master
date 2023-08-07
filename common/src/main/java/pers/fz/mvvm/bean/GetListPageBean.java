package pers.fz.mvvm.bean;

/**
 * Create by CherishTang on 2019/10/24 0024
 * describe:
 */
public class GetListPageBean {
    private int pageNum;
    private int pageSize;
    private String keywords;

    public GetListPageBean(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public GetListPageBean(int pageNum, int pageSize, String keywords) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.keywords = keywords;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

}
