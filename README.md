# Gym Kappa – Gym Management System

A full-stack gym management application built with a Spring Boot REST API backed by a relational MySQL database, and a HTML/CSS/JavaScript frontend. It handles everything a small gym needs day to day: member registration and approval, a trainer and service catalog, a weekly class schedule, and a reservation system — split into a full admin back office and a member-facing booking flow.

## Table of Contents

1. [Additional Assumptions / Business Rules](#additional-assumptions--business-rules)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [Project Structure](#project-structure)
5. [How to Run the System](#how-to-run-the-system)
6. [How to Use the System](#how-to-use-the-system)

## Additional Assumptions / Business Rules

- New accounts require admin approval before the user can log in; requests can be accepted or rejected.
- Each scheduled class (`program`) belongs to exactly one service and one trainer.
- A trainer cannot be double-booked within the same hour on the same day.
- A member cannot cancel a reservation less than 2 hours before the class starts, or after it has already happened.
- Deleting a trainer or service removes their **future** scheduled classes (and any reservations for them), but past classes are kept intact so members can still see them in their reservation history.
- Deleting a user or a class deletes its associated reservations along with it.

## Features

**Administrator**
- Review, accept, or reject pending registrations
- Manage member accounts (edit details, change role, delete)
- Full CRUD on trainers, services (with image upload), and the class schedule, with automatic trainer double-booking prevention
- Publish, edit, and delete announcements

**Member**
- Register and log in
- Browse available services, each with its own weekly class schedule
- Book and cancel reservations for upcoming classes
- View reservation history
- Read gym announcements

## Technologies Used

- Java 23, Spring Boot (Spring Web, Spring Data JPA)
- MySQL 8
- Hibernate / JPA
- Docker & Docker Compose
- HTML, CSS, JavaScript (no frontend framework)
- Maven

## Project Structure

### Backend (`com.example.API`)
- **models/** – JPA entities: `Trainers`, `Services`, `Users`, `Articles`, `Programs`, `UserReservations`
- **Repositories/** – Spring Data JPA repositories for each entity
- **dto/** – `ProgramDTO` / `ReservationDTO`, decoupling the relational model from the flat JSON shape the frontend consumes
- **UsersController.java** – all REST endpoints
- **WebConfig.java** – CORS and static resource (uploaded images) configuration
- **ApiApplication.java** – Spring Boot entry point

### Frontend
- **Admin/** – `Manage_System`, `Manage_Users`, `Manage_Announcements`, `Registration_Requests`
- **User/** – `Show_Services`, `Product_Reservations`, `Show_History`, `Show_Announcements`
- **Login_Register/** – shared login/signup page
- **navigation_bar.js** – shared navigation bar, driven by the logged-in user's role

## How to Run the System

**1. Start the database**
```bash
docker compose up -d
```
This starts a MySQL 8 container matching the credentials in `application.properties`.

**2. Run the backend**
Open the project in IntelliJ (or run via Maven) and start `ApiApplication`. On first run against an empty database, Hibernate creates the schema automatically.

**3. Load sample data (optional)**
```bash
docker exec -i gym-mysql mysql --default-character-set=utf8mb4 -u root -psecret gym_db < seed_data.sql
```
> **Note:** signing up through the app can only ever produce a regular member — approval always assigns the `user` role, and promoting someone to `admin` requires an admin to already be logged in to do it. On a fresh database with no sample data, there's no self-service way to create the first admin account. Either load the sample data above, which includes a ready-to-use demo admin login (`admin` / `admin123`), or register normally and promote yourself directly in the database: `UPDATE users SET role='admin', accepted=1 WHERE username='yourusername';`

**4. Serve the frontend**
The frontend is a set of static HTML/JS files that call the API at `localhost:8080`. Serve them through a local static server (e.g. IntelliJ's built-in web server) rather than opening the files directly, so cookies and CORS behave consistently. Start at `Login_Register/newuser.html`.

## How to Use the System

### As Administrator
1. Log in with an admin account.
2. Available sections:
   - **Registration Requests** – accept or reject new sign-ups
   - **Manage Users** – edit or remove existing members
   - **Manage System** – add/edit/delete trainers, services, and scheduled classes
   - **Announcements** – publish gym news and offers

### As Member
1. Sign up, then wait for admin approval before logging in.
2. Available sections:
   - **Services** – browse services and each one's schedule for the current week
   - **Make a Reservation** – book an open class, or cancel an existing one (up to 2 hours before it starts)
   - **Reservation History** – view past bookings
   - **Announcements** – read gym news
