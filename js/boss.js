/*
 * boss.js
 * created on 12/01/2010
 * Copyright (C) 2010 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 * http://www.gnu.org/copyleft/lesser.html
 */
var gaggle = {};

gaggle.GooseProxy = function(uniqueId) {
    this.getId = function() { return uniqueId; };
};

// all geese have the method getId()
// and sendMessage()

gaggle.Boss = function() {
    var geese = [];
    var log = []; // a log of messages

    /*
     * A simple logging function, for diagnostic purposes. Using this method assumes that
     * jQuery >= 1.4.3 is used within the page
     */
    this.log = function(source, msg) {
        var logMsg = '<span style="color: blue;">' + source +
            ':</span> \'' + msg + "\'";
        log.push(logMsg);
        var logText = '<br>';
        for (var i = 0; i < log.length; i++) {
            logText += log[i] + '<br>';
        }
        $('#gaggle-log-text').replaceWith('<div id="gaggle-log-text">' + logText + '</div>');
    };
    this.createProxy = function (gooseBaseName) {
        var uniqueId = gooseBaseName;
        var proxy = new gaggle.GooseProxy(uniqueId);
        this.log('boss', 'created proxy with id: ' + uniqueId);
        this.register(proxy);
        return uniqueId;
    };
    this.register = function(goose) {
        if (this.exists(goose.getId())) throw "goose with id '" + goose.getId() + "' exists already.";
        geese[goose.getId()] = goose;
        this.log('boss', 'registered goose with id: ' + goose.getId());
    };
    this.unregister = function(gooseId) {
        geese[gooseId] = undefined;
        this.log('boss', 'unregistered goose with id: ' + gooseId);
    };
    this.exists = function(gooseId) {
        return geese[gooseId] !== undefined;
    };
};

