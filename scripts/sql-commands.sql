--- Create table for suggestions.

CREATE TABLE "phrases" ("original" TEXT,"translation" TEXT,"directProbability" TEXT, "lexticalWeighting" TEXT);

--- Copy data.

COPY phrases FROM '/Users/JurajPancik/School/bakalarka/project/data/dbdata.1.csv' delimiter ',' CSV;

--- Create index.

--- CREATE index phrase_idx ON phrases(original);
CREATE index phrase_idx ON phrases USING HASH (original);

--- Example select.

SELECT * FROM phrases WHERE phrases.original='dog';