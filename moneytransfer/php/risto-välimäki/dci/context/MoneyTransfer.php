<?php
/**
 * MoneyTransfer Context
 */
class MoneyTransfer {
    // MoneyTransfer Use Case:
    // 1. user starts money transfer, and is displayed with money transfer form
    // 2. user then selects source account, destination account and amounnt of money to be sent
    // 3. user commits moneytransfer, system verifies
    // 4. system write logs about transfer

    function start() {
        //find all possible source accounts for current user
        $v['sourceAccounts'] = Account::find_all_by_user_id(USER_ID);
        return $v; // v is just short for variables, a temporary table where all the view vars are stored
    }

    // this function looks messy, and actually does much more than only verify...
    function verify() {
        if($_POST['SourceAccount']['number'] == $_POST['DestinationAccount']['number']) {
            $v['message'] = 'Source and Destination are same'; return $v;
        }

        $source = new SourceAccount(Account::find_by_number($_POST['SourceAccount']['number']));
        if(!$source) { $v['message'] = 'Invalid Source Account'; return $v; }
        
        $destination = new DestinationAccount(Account::find_by_number($_POST['DestinationAccount']['number']));
        if(!$destination) { $v['message'] = 'Invalid Destination Account'; return $v; }

        $amount = $_POST['MoneyTransfer']['amount'];
        if($amount <= 0) { $v['message'] = 'Invalid Amount'; return $v; }

        if($source->drawMoney($amount) && $destination->deposit($amount)) {
            if($source->save() && $destination->save()) {
                $this->log($source, $destination, $amount);

                $v['message'] = 'Success';
                return $v;
            }
            $v['message'] = 'Failed';
        }
        $v['message'] = $source->errors('amount');
        return $v;
    }


    private function log($source, $destination, $amount) {
        Transfer::create(array(
            'source_account_number'=>$source->d()->number,
            'destination_account_number'=>$destination->d()->number,
            'amount'=>$amount,
            'user_id'=>USER_ID,
            'created'=>new DateTime())
        );
    }

}

/**
 * These roles are so simple, that I could ignore them and use Medator pattern instead.
 */
class SourceAccount extends Role {
    function drawMoney($amount) {
        $this->data->amount -= $amount;
        return $this->data->is_valid();
    }
}

class DestinationAccount extends Role {
    function deposit($amount) {
        $this->data->amount += $amount;
        return $this->data->is_valid();
    }
}
?>