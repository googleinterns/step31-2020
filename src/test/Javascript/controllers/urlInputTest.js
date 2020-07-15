var Jasmine = require('jasmine');
var jasmine = new Jasmine();

jasmine.loadConfigFile('spec/support/jasmine.json');

jasmine.execute();

describe('The application controller', function() {
  it("should explicitly fail", function () { fail('Forced to fail'); });
});
