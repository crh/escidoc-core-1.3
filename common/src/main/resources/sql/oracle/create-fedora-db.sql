/**
 * Database initialization
 * Prerequisite: Created Oracle Database with Characterset UTF8
 */
/**
 * create fedora DB users
 */  
CREATE USER ${fedora.database.user} 
IDENTIFIED BY ${fedora.database.password}
DEFAULT TABLESPACE ${fedora.default.tablespace}
TEMPORARY TABLESPACE ${fedora.temp.tablespace}; 

GRANT CONNECT, create any table, create any index, create any sequence, create any trigger, drop any table, drop any index, drop any sequence, drop any trigger, select any table, insert any table, update any table, delete any table, alter any sequence, alter any trigger, UNLIMITED TABLESPACE TO ${fedora.database.user};

/**
 * create triplestore DB users
 */  
CREATE USER ${triplestore.database.user} 
IDENTIFIED BY ${triplestore.database.password}
DEFAULT TABLESPACE ${triplestore.default.tablespace}
TEMPORARY TABLESPACE ${triplestore.temp.tablespace}; 

GRANT CONNECT, create any table, create any index, create any sequence, create any trigger, drop any table, drop any index, drop any sequence, drop any trigger, select any table, insert any table, update any table, delete any table, alter any sequence, alter any trigger, UNLIMITED TABLESPACE TO ${triplestore.database.user};
 
