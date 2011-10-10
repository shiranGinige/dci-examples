<?php
/**
 * YourAccount Context
 */
class YourAccount {
    function index() {
        $v['accounts'] = Account::find_all_by_user_id(USER_ID);
        $v['transfers'] = Transfer::find_all_by_user_id(USER_ID);
        return $v;
    }
}
?>
