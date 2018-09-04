from('timer:js')
    .setBody('test')
    .setHeader('MyHeader', 'MyValue')
    .to('log:js')