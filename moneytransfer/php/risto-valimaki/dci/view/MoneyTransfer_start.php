<form action="../MoneyTransfer/verify" method="post">

<p>Source Account:</p>
<select name="SourceAccount[number]">
    <? foreach($sourceAccounts as $s): ?>
    <option value="<?=$s->number?>"><?=$s->number?> <?=$s->name?> (<?=$s->amount?>€)</option>
    <? endforeach; ?>
</select>
<p>Destination Account:</p>
<input name="DestinationAccount[number]" type="text" />
<p>Amount:</p>
<input name="MoneyTransfer[amount]" type="text" />
<input type="submit" value="Submit" />
</form>