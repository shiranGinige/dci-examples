<?php
/**
 * superclass for Role objects.
 */
class Role {
    /**
     * Data object that's wrapped with this Role object.
     * Data is the real thing, Role is only a cloak!
     */
    var $data;

    public function __construct($data) {
        $this->data = $data;
    }
    public function d() {
        return $this->data;
    }

    //these two are probably too much related with
    public function save() {
        return $this->data->save();
    }

    public function errors($field = null) {
        $errors = $this->data->errors;
        if($field)
            return $errors->on($field);
        return $errors;
    }
}
?>