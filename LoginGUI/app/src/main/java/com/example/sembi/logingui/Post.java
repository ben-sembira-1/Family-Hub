package com.example.sembi.logingui;

import java.util.Date;

class Post {
    private String mPublisherStr;
    private String mContentStr;
    private Date mPublishDate;
    private String mLinkStr;

    public Post(String mPublisherStr, String mContentStr, Date mPublishDate, String mLinkStr) {
        this.mPublisherStr = mPublisherStr;
        this.mContentStr = mContentStr;
        this.mPublishDate = mPublishDate;
        this.mLinkStr = mLinkStr;
    }

    public Post() {
    }

    public String getmImagePathStr() {
        return getmPublishDate().toString() + "%" + getmPublisherStr();
    }

    public String getmPublisherStr() {
        return mPublisherStr;
    }

    public void setmPublisherStr(String mPublisherStr) {
        this.mPublisherStr = mPublisherStr;
    }

    public String getmContentStr() {
        return mContentStr;
    }

    public void setmContentStr(String mContentStr) {
        this.mContentStr = mContentStr;
    }

    public Date getmPublishDate() {
        return mPublishDate;
    }

    public void setmPublishDate(Date mPublishDate) {
        this.mPublishDate = mPublishDate;
    }

    public String getmLinkStr() {
        return mLinkStr;
    }

    public void setmLinkStr(String mLinkStr) {
        this.mLinkStr = mLinkStr;
    }
}
