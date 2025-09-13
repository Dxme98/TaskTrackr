CREATE TABLE IF NOT EXISTS basic_details (
    id BIGSERIAL primary key,
    FOREIGN KEY (id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scrum_details (
    id BIGSERIAL primary key,
    FOREIGN KEY (id) REFERENCES projects(id) ON DELETE CASCADE
)