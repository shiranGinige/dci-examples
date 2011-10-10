Simple DCI -- Data, Context and Interaction (or DCIV/DVC, "Data-View-Context") framework and example app for PHP

Version 0.01 07.07.2010

Licencing / Authors:
-Uses heavily php-activerecord 1.0 -AR/ORM -framework (lib/php-activerecord/*, MIT-style licenced.)
-Template engine (18 lines of code!) borrowed from http://www.massassi.com/php/articles/template_engines/ 
(dci/lib/Template.php, Public Domain)
-All other code in dci -folder (if not stated else) Copyright Risto Välimäki (risto.valimaki@gmail.com). Licence LGPL.

Framework is still so simple that you can probably find out how it works. 

Stick with strict naming convention!

Data:
*files in 'data' filenames like "SingularCamelCase.php" when Data class name is "SingularCamelCase"
sql-database table names:
*"underscored_plural_tables". For other Data-related conventions check out php-activerecord wiki: 
http://www.phpactiverecord.org/projects/main/wiki/Conventions

Context:
*files in 'context' and filenames SingularCamelCase as class names too.

Interaction (Roles):
*definitions in Context files, for example "class SourceAccount extends Role {}" in "context/MoneyTransfer.php"
*Roles should be defined after the Context class.

View:
*view/ContextName_action.php, where "ContextName" is name of a context and "action" is one of its functions
*for example "MoneyTransfer::start()"->"MoneyTransfer_start.php"

Layouts:
*for example view/layout/default.php

Quick startup:
-have Apache mod_rewrite and PDO for your database installed
-configure your database in config.php (username, password and database name, currently "test")
-run sql queries from doc/test.sql in to your (mysql) database
-extract dci and lib -directories into your www-root (may be /var/www or /srv/http for example)
-navigate to "http://localhost/dci/", you should be on "localhost/dci/YourAccount/index". 
-enjoy transfering virtual money...

Risto Välimäki