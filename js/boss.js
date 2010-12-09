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

gaggle.GooseProxy = function(bridgeBoss, uniqueId) {
    this.getId = function() { return uniqueId; };
    this.getName = function() { return uniqueId; }
    this.update = function(currentGooseIds) {
        bridgeBoss.updateGoose(uniqueId, currentGooseIds);
    };
};

// all geese have the method getId()
// and sendMessage()

gaggle.Boss = function(bridgeBoss) {
    var gooseUIDs = [];
    var gooseMap = [];
    var log = []; // a log of messages

    this.uniqueNameFor = function(baseName) {
        var maxCount = 0;
        var regexp = new RegExp(baseName + '-(\\d)+');

        for (var i = 0; i < gooseUIDs.length; i++) {
            if (gooseUIDs[i].match(regExp)) {
                var comps = gooseUIDs[i].split('-');
                var count = parseInt(comps[comps.length - 1]);
                if (count > maxCount) maxCount = count;
            }
        }
        var uniqueCount = maxCount + 1;
        return (uniqueCount < 10) ? baseName + '-0' + uniqueCount : baseName + '-' + uniqueCount;
    };
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
        var proxy = new gaggle.GooseProxy(bridgeBoss, uniqueId);
        this.log('boss', 'created proxy with id: ' + uniqueId);
        this.register(proxy);
        return uniqueId;
    };

    // service interface
    this.register = function(goose) {
        if (this.exists(goose.getName())) throw "goose with id '" + goose.getName() + "' exists already.";
        gooseUIDs[gooseUIDs.length] = goose.getName();
        gooseMap[goose.getName()] = goose;
        for (var i = 0; i < gooseUIDs.length; i++) {
            gooseMap[goose.getName()].update(gooseUIDs);
        }
        this.log('boss', 'registered goose with name: ' + goose.getName());
    };
    this.unregister = function(gooseId) {
        gooseMap[gooseId] = undefined;
        var index = gooseUIDs.indexOf(gooseId);
        if (index >= 0) { gooseUIDs.splice(index, 1); }
        this.log('boss', 'unregistered goose with id: ' + gooseId);
    };
    this.exists = function(gooseId) {
        return gooseMap[gooseId] !== undefined;
    };
    this.numGeese = function() {
        return gooseUIDs.length;
    };
};

