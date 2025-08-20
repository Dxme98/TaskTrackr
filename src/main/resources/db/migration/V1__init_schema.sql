--  1. Look up Tabellen
CREATE TABLE IF NOT EXISTS project_type (
    id SERIAL PRIMARY KEY,
    name varchar(32) UNIQUE
);

CREATE TABLE IF NOT EXISTS invite_status (
    id SERIAL PRIMARY KEY,
    name varchar(32) UNIQUE
);


-- 2. Kern-Tabellen
CREATE TABLE IF NOT EXISTS users (
    id varchar(36) primary key,
    username varchar(32) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL primary key,
    name varchar(255) NOT NULL,
    creator_id varchar(36),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    project_type_id INT NOT NULL,

    FOREIGN KEY (project_type_id) REFERENCES project_type(id) ON DELETE RESTRICT,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE SET NULL
);


-- 3. Relationen
CREATE TABLE IF NOT EXISTS project_invite (
    id BIGSERIAL primary key,
    receiver_id varchar(36) NOT NULL ,
    sender_id varchar(36) NOT NULL ,
    project_id BIGINT NOT NULL ,
    invite_status_id INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(receiver_id, sender_id, project_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE ,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (invite_status_id) REFERENCES invite_status(id)
);

CREATE TABLE IF NOT EXISTS project_member (
    user_id varchar(36) NOT NULL,
    project_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, project_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Indexe -- Optimierung mit verschiedenen Index Varianten am Ende
CREATE INDEX idx_project_invite_receiver_id ON project_invite(receiver_id, invite_status_id); -- Schnelle Query für erhaltene offene Einladungen
CREATE INDEX idx_project_invite_sender_id ON project_invite(sender_id, invite_status_id); -- Schnelle Query für gesendete Einladungen
CREATE INDEX idx_project_member_project_id ON project_member(project_id); -- Schnelle Query zum Auflisten aller ProjectMember
CREATE INDEX idx_users_username ON users(username);

INSERT INTO invite_status (id, name) VALUES
    (1, 'PENDING'),
    (2, 'ACCEPTED'),
    (3, 'DECLINED')
ON CONFLICT DO NOTHING;

INSERT INTO project_type (id, name) VALUES
     (1, 'BASIC'),
     (2, 'SCRUM')
ON CONFLICT DO NOTHING;