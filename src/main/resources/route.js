from('timer:js?period=5s')
    .setBody('test')
    .to('log:js')