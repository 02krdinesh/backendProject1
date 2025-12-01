
# **Charity Management System – Spring Boot Backend**

A web-based backend application built with **Spring Boot** to manage charity operations, donations, beneficiaries, events, and user roles. This system provides a robust REST API that can be integrated with any frontend framework (React, Angular, Vue, etc.).

---

## **Features**

### **Authentication & Authorization**

* User registration and login
* Role-based access control (Admin, Donor, Staff)

### **Donation Management**

* Add and track donations
* Link donations to donors and campaigns
* Generate donation history and summaries

### **Beneficiary Management**

* Register, update, and manage beneficiaries
* Assign donations or resources to beneficiaries

### **Campaign & Event Management**

* Create and manage charity campaigns or fundraising events
* Track campaign progress

### **Dashboard & Reporting**

* View overall statistics
* Donation trends
* Beneficiary support overview

---

## 🛠️ **Tech Stack**

| Layer             | Technology            |
| ----------------- | --------------------- |
| Backend Framework | Spring Boot           |
| Security          | Spring Security + JWT |
| Database          | MySQL / PostgreSQL    |
| ORM               | Hibernate / JPA       |
| Build Tool        | Maven / Gradle        |

---

## **Project Structure (Typical)**

```
src/main/java/com/example/charity
│
├── controller        # REST controllers
├── service           # Business logic
├── repository        # JPA repositories
├── model             # Entity classes
├── config            # Security & configuration
└── dto               # Data Transfer Objects
```

## **NOTE**

**This project is not intended for production use.**

---
