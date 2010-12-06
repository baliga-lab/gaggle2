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

// all geese have the method getId()
// and sendMessage()

gaggle.Boss = function() {
    var geese = [];
    var log = []; // a log of messages

    this.broadcast = function(sourceId, msg) {
        var logMsg = 'Msg from <span style="color: blue;">' + sourceId +
            ':</span> \'' + msg + "\'";
        log.push(logMsg);
        var logText = '<br>';
        for (var i = 0; i < log.length; i++) {
            logText += log[i] + '<br>';
        }
        $('#akka-log-text').replaceWith('<span id="akka-log-text">' + logText + '</span>');
        // notify geese except source
        for (var id in geese) {
            if (id != sourceId) {
                geese[id].sendMessage(msg);
            }
        }
    };
    this.register = function(goose) {
        if (this.exists(goose.getId())) throw "goose with id '" + goose.getId() + "' exists already.";
        geese[goose.getId()] = goose;
    };
    this.unregister = function(gooseId) {
        geese[gooseId] = undefined;
    };
    this.exists = function(gooseId) {
        return geese[gooseId] !== undefined;
    };
};

