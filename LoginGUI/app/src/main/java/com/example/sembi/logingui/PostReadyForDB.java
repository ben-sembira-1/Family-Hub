package com.example.sembi.logingui;

class PostReadyForDB {
    private String mPublisherStr;
    private String mContentStr;
    private String mLinkStr;
    private String mImagePathStr;

    public PostReadyForDB(String mPublisherStr, String mContentStr, String mLinkStr, String mImagePathStr) {
        this.mPublisherStr = mPublisherStr;
        this.mContentStr = mContentStr;
        this.mLinkStr = mLinkStr;
        this.mImagePathStr = mImagePathStr;
    }

    public PostReadyForDB(Post post) {
        this.mContentStr = post.getmContentStr();
        this.mLinkStr = post.getmLinkStr();
        this.mPublisherStr = post.getmPublisherStr();
        this.mImagePathStr = post.getmPublishDate().toString() + "%" + post.getmPublisherStr();
    }

    public PostReadyForDB() {
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

    public String getmLinkStr() {
        return mLinkStr;
    }

    public void setmLinkStr(String mLinkStr) {
        this.mLinkStr = mLinkStr;
    }

    public String getmImagePathStr() {
        return mImagePathStr;
    }

    public void setmImagePathStr(String mImagePathStr) {
        this.mImagePathStr = mImagePathStr;
    }
}
