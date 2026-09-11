package com.br.api.dto.company;

public class CompanyDTO {
    private Long id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String logoData;
    private String printFooter;

    public CompanyDTO() {
    }

    public CompanyDTO(com.br.api.model.Company company) {
        this.id = company.getId();
        this.name = company.getName();
        this.document = company.getDocument();
        this.email = company.getEmail();
        this.phone = company.getPhone();
        this.address = company.getAddress();
        this.city = company.getCity();
        this.state = company.getState();
        this.logoData = company.getLogoData();
        this.printFooter = company.getPrintFooter();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getLogoData() { return logoData; }
    public void setLogoData(String logoData) { this.logoData = logoData; }
    public String getPrintFooter() { return printFooter; }
    public void setPrintFooter(String printFooter) { this.printFooter = printFooter; }
}