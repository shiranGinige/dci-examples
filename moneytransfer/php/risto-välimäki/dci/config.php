<?php
date_default_timezone_set('UTC');
require_once '../lib/php-activerecord/ActiveRecord.php';
require_once 'lib/Data.php';
require_once 'lib/Role.php';
require_once 'lib/Template.php';

ActiveRecord\Config::initialize(function($cfg)
{
    $cfg->set_model_directory(dirname(__FILE__) . '/data');
    $cfg->set_connections(array('development' => 'mysql://username:password@localhost/test'));
});
?>