-- 1. Core Users & Security
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT',
    status ENUM('ACTIVE', 'SUSPENDED', 'DEACTIVATED') DEFAULT 'ACTIVE',
    is_email_verified BOOLEAN DEFAULT FALSE,
    college VARCHAR(255),
    course VARCHAR(255),
    year VARCHAR(50),
    phone VARCHAR(50),
    bio TEXT,
    photo_url VARCHAR(255),
    github_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose ENUM('REGISTRATION', 'PASSWORD_RESET') NOT NULL,
    attempts INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    INDEX (email)
);

-- 2. Skills System
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE user_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE (user_id, skill_id)
);

-- 3. Opportunities & Roles
CREATE TABLE opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(50),
    status ENUM('OPEN', 'CLOSED', 'TEAM_FORMED', 'COMPLETED', 'ARCHIVED', 'SUSPENDED') DEFAULT 'OPEN',
    mode ENUM('ONLINE', 'OFFLINE', 'HYBRID'),
    location VARCHAR(255) NOT NULL,
    link_type ENUM('REGISTRATION', 'OFFICIAL_WEBSITE', 'MORE_INFORMATION', 'OTHER'),
    external_link VARCHAR(255),
    start_date DATE,
    end_date DATE,
    application_deadline TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE opportunity_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opportunity_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    vacancies INT DEFAULT 1,
    filled_count INT DEFAULT 0,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CHECK (vacancies > 0),
    CHECK (filled_count >= 0),
    CHECK (filled_count <= vacancies)
);

CREATE TABLE role_requirement_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES opportunity_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE (role_id, skill_id)
);

-- 4. Applications & Teams
CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES opportunity_roles(id) ON DELETE CASCADE,
    UNIQUE (student_id, opportunity_id, role_id)
);

CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opportunity_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    UNIQUE (opportunity_id)
);

CREATE TABLE team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    role_id BIGINT NULL,
    member_type ENUM('ORGANIZER', 'MEMBER') DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES opportunity_roles(id) ON DELETE SET NULL,
    UNIQUE (team_id, student_id)
);

-- 5. Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('NEW_APPLICATION', 'APPLICATION_ACCEPTED', 'APPLICATION_REJECTED', 'APPLICATION_CANCELLED', 'ADDED_TO_TEAM', 'OPPORTUNITY_CLOSED') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Trust, Safety & Support
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NULL,
    reported_opportunity_id BIGINT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
    reviewed_by_id BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    CHECK ((reported_user_id IS NOT NULL AND reported_opportunity_id IS NULL) OR (reported_user_id IS NULL AND reported_opportunity_id IS NOT NULL))
);

CREATE TABLE support_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('BUG', 'SUPPORT', 'FEEDBACK', 'FEATURE_REQUEST') NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
    reviewed_by_id BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL
);
