<p>Your accounts:</p>

<table>
    <tr>
        <th>Name</th>
        <th>Number</th>
        <th>Amount (€)</th>
    </tr>
<? foreach($accounts as $t): ?>
    <tr>
        <td><?= $t->name ?></td>
        <td><?= $t->number ?></td>
        <td><?= $t->amount ?></td>
    </tr>
<? endforeach; ?>

</table>

<p>Your transfers:</p>

<table>
    <tr>
        <th>Source Account</th>
        <th>Destination Account</th>
        <th>Amount (€)</th>
        <th>Date</th>
    </tr>
<? foreach($transfers as $t): ?>
    <tr>
        <td><?= $t->source_account_number ?></td>
        <td><?= $t->destination_account_number ?></td>
        <td><?= $t->amount ?></td>
        <td><?= $t->created->format('Y-m-d H:i') ?></td>
    </tr>
<? endforeach; ?>

</table>

<p><a href="/dci/MoneyTransfer/start">Add new transfer</a></p>