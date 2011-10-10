<?php
/**
 * Dispatcher for every url request in this app. Brings up right Context by "RESTful" url, like:
 * http://your.domain.com/MoneyTransfer/start
 * ->
 * context = use case = MoneyTransfer
 * action = use case step = start 
 */

require_once 'config.php';

define('USER_ID', 1);

$url = split('/', $_GET['url']);

$context_name = (isset($url[0]) && $url[0] != '' ? $url[0] : 'YourAccount'); // ie. "MoneyTransfer"
$action =(isset($url[1]) && $url[1] != '' ? $url[1] : 'index');; // ie. "index";

//Selecting right Context (Use Case) and Action (Use Case step)
require_once 'context/' . $context_name . '.php';
$context = new $context_name();
$view_vars = $context->$action();

$view_name = 'view/' . $context_name . '_' . $action . '.php';

$view = & new Template($view_name);
foreach ($view_vars as $key => $value) {
    $view->set($key, $value);
}

$layout_name = 'view/layout/default.php';
if (isset($view_vars['layout'])) {
    if ($view_vars['layout'] == false)
        $layout_name = false;
    elseif ($view_vars['layout'] != '')
        $layout_name = 'view/layouts/' . $view_vars['layout'] . 'php';
}

// if (wrapper) layout is defined, show it
if ($layout_name) {
    $layout = & new Template($layout_name);
    $layout->set('view', $view);
    echo $layout->fetch();
}
// sometimes, especially with AJAX you do not wan't to use any wrapper layout, so show only the view
else {
    echo $view->fetch();
}
?>