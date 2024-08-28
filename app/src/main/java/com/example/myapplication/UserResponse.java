package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {

    @SerializedName("page")
    private int page;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("total")
    private int total;

    @SerializedName("total_pages")
    private int totalPages;  // Add this field

    @SerializedName("data")
    private List<User> data;

    // Getter and Setter for page
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    // Getter and Setter for perPage
    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    // Getter and Setter for total
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // Getter and Setter for totalPages
    public int getTotalPages() {  // Ensure this method is defined
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    // Getter and Setter for data
    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }
}
