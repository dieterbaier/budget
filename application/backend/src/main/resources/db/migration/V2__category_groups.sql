-- Category groups, and the group every category now belongs to (issue #8).
--
-- The unique indexes below are not business rules. The domain identifies a group
-- and a category by name (ADR-009), so the outbound port is a store keyed by
-- name; a unique index is how a relational adapter delivers that, in the same way
-- a HashMap uses hashing. Nothing catches a constraint violation to decide
-- anything -- if one ever fires, the domain failed to enforce its own rule and
-- that is a defect (ADR-021).
--
-- The foreign keys are the same thing for references: the domain declares that a
-- category belongs to a group and that a transaction has a category, and the
-- adapter implements those. The rule that a category in use cannot be deleted is
-- enforced by the use case, which queries the transaction and fixed-cost ports
-- and produces the message the owner reads.

create table category_groups (
    id   bigint       generated always as identity primary key,
    name varchar(100) not null unique
);

-- Existing categories predate grouping. They are backfilled into one group
-- rather than guessed at: the owner renames and regroups them from the UI, which
-- is exactly what this slice makes possible.
insert into category_groups (name) values ('Ungrouped');

alter table categories
    add column group_id bigint references category_groups (id);

update categories
set group_id = (select id from category_groups where name = 'Ungrouped')
where group_id is null;

alter table categories
    alter column group_id set not null;

create index idx_categories_group_id on categories (group_id);
