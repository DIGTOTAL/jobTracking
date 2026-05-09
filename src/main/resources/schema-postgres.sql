-- PostgreSQL schema init (safe to run multiple times)
-- NOTE: no DROP statements here.

CREATE TABLE IF NOT EXISTS employees (
    name VARCHAR(255) PRIMARY KEY,
    work_experience INT NOT NULL
);

CREATE TABLE IF NOT EXISTS employee_skills (
    employee_name VARCHAR(255) NOT NULL,
    skill VARCHAR(255) NOT NULL,
    PRIMARY KEY (employee_name, skill)
);

CREATE TABLE IF NOT EXISTS vacancies (
    vacancy_name VARCHAR(255) PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    required_work_experience INT NOT NULL
);

CREATE TABLE IF NOT EXISTS vacancy_tags (
    vacancy_name VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (vacancy_name, tag)
);

-- Foreign keys.
-- Spring's SQL initializer splits scripts by ';' and doesn't handle DO $$ ... $$ blocks well,
-- so we rely on continue-on-error to ignore "already exists" errors on repeated runs.
ALTER TABLE employee_skills
    ADD CONSTRAINT fk_employee_skills_employee
    FOREIGN KEY (employee_name) REFERENCES employees(name)
    ON DELETE CASCADE;

ALTER TABLE vacancy_tags
    ADD CONSTRAINT fk_vacancy_tags_vacancy
    FOREIGN KEY (vacancy_name) REFERENCES vacancies(vacancy_name)
    ON DELETE CASCADE;
