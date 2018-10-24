from('timer:js?period=1s')
    .setBody('test')
    .to('log:js?showAll=false&multiline=false')