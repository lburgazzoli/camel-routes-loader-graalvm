from('timer:js?period=1s')
    .setBody().constant('test')
    .process(e => e.getMessage().setBody('test'))
    .to('log:js?showAll=false&multiline=false')