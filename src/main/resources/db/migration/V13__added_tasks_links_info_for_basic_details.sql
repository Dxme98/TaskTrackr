CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE task_status AS ENUM ('IN_PROGRESS', 'EXPIRED', 'COMPLETED');
CREATE TYPE link_type AS ENUM ('GITHUB', 'DOCS', 'WEB');

CREATE TABLE IF NOT EXISTS tasks (
      id BIGSERIAL PRIMARY KEY,
      project_id BIGINT NOT NULL,
      title VARCHAR(255) NOT NULL,
      description TEXT NOT NULL CHECK (LENGTH(description) <= 2000),
      priority task_priority NOT NULL,
      status task_status NOT NULL DEFAULT 'IN_PROGRESS',
      created_at TIMESTAMP NOT NULL,
      updated_at TIMESTAMP NOT NULL,
      due_date TIMESTAMP,
      created_by BIGINT,
      updated_by BIGINT,

      FOREIGN KEY (project_id) REFERENCES basic_details(project_id) ON DELETE CASCADE,
      FOREIGN KEY (created_by) REFERENCES project_member(id) ON DELETE SET NULL,
      FOREIGN KEY (updated_by) REFERENCES project_member(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS task_assignments (
      task_id BIGINT NOT NULL,
      member_id BIGINT NOT NULL,
      PRIMARY KEY (task_id, member_id),

      FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
      FOREIGN KEY (member_id) REFERENCES project_member(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS links (
      id BIGSERIAL PRIMARY KEY,
      title VARCHAR(80) NOT NULL,
      url VARCHAR(500) NOT NULL,
      type link_type NOT NULL,
      project_id BIGINT NOT NULL,

      UNIQUE(project_id, title),
      FOREIGN KEY (project_id) REFERENCES basic_details(project_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS information (
      id BIGSERIAL PRIMARY KEY,
      project_id BIGINT NOT NULL,
      content TEXT NOT NULL CHECK (LENGTH(content) <= 100000),

      FOREIGN KEY (project_id) REFERENCES basic_details(project_id) ON DELETE CASCADE
);