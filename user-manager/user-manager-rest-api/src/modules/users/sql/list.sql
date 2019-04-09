/**
 * Returns a paginated list of users
 */
SELECT user_id AS "userId",
    email,
    firstname,
    lastname,
    enabled,
    created,
    modified
FROM ${schema~}.user
LIMIT ${limit}
OFFSET ${offset}

