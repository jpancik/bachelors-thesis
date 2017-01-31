class TreeNode {
    constructor(phrases, translated, children) {
        this.phrases = phrases;
        this.translated = translated;
        this.children = children;
        this.probability = 1.0;
    }

    addPhrase(phrase) {
        this.phrases.push(phrase);
        for (var i = phrase.beginIndex; i <= phrase.endIndex; i++) {
            this.translated[i] = true;
        }
        this.probability *= phrase.directProbability;
    };

    generateChildren(serverPhrases, iterations, prefixWord) {
        // Generate all next phrases for this node.
        var phrases = serverPhrases;
        if (prefixWord != null) {
            /*
             var filtered = serverPhrases.filter(function (phrase) {
             var start = Math.max(prefixWords.length - phrase.translation.length, 0);
             for (; start < prefixWords.length; start++) {
             var ok = true;
             for (var i = start; i < prefixWords.length; i++) {
             if (phrase.translation[i - start] != prefixWords[i]) {
             ok = false;
             break;
             }
             }
             if (ok) {
             return true;
             }
             }
             });
             */
            var filtered = serverPhrases.filter(function (phrase) {
                return phrase.translation.startsWith(prefixWord);
            });

            if (filtered.length > 0) {
                console.log("Filtered phrases by prefix filter: " + prefixWord + " with length: " + filtered.length);
                console.log(filtered);
                phrases = filtered;
            }
        }

        var newChildren = [];
        for (var j = 0; j < phrases.length; j++) {
            var phrase = phrases[j];
            var ok = true;
            for (var i = phrase.beginIndex; i <= phrase.endIndex; i++) {
                if(this.translated[i] == true) {
                    ok = false;
                    break;
                }
            }
            if(!ok) {
                continue;
            }

            var nextNode = new TreeNode(
                this.phrases.slice(0),
                this.translated.slice(0),
                []
            );

            nextNode.addPhrase(phrase);

            newChildren.push(nextNode);

            if(newChildren.length >= 50) {
                break;
            }
        }
        this.children = newChildren;

        // Trim sorted phrases to top X.
        this.children.sort(function (a, b) {
            return b.probability - a.probability;
        });
        this.children = this.children.slice(0, 50);

        if (iterations - 1 > 0 || this.children.length == 0) {
            // Generate next phrases for all new children.
            for (var j = 0; j < this.children.length; j++) {
                this.children[j].generateChildren(serverPhrases, iterations - 1, null);
            }
        }
    };

    getLastStepChildren() {
        if(this.children.length == 0) {
            return [this];
        } else {
            var out = [];
            for (var j = 0; j < this.children.length; j++) {
                var children = this.children[j].getLastStepChildren();
                for (var i = 0; i < children.length; i++) {
                    out.push(children[i]);
                }
            }
            return out;
        }
    };

    getLastPhrase() {
        if (this.phrases.length == 0) {
            return null;
        } else {
            return this.phrases[this.phrases.length - 1];
        }
    };
}

function buildTree(phrases, treeRoot, prefixWord) {
    var startTime = Date.now();

    // Sort server phrases by probability.
    phrases.sort(function (a, b) {
        return b.directProbability - a.directProbability;
    });

    var out = treeRoot;
    if (out == null) {
        var originalPhraseLength = originalPhrase.split(" ").length;
        out = new TreeNode([], [], []);
        for (var i = 0; i < originalPhraseLength; i++) {
            out.translated.push(false);
        }
    }
    out.generateChildren(phrases, 2, prefixWord);

    var elapsedTime = Date.now() - startTime;
    console.log("Build tree timing: " + elapsedTime);

    return out;
}

function groupAndSortTreeNodesByPhraseTranslation(index, treeNodes) {
    var firstPhraseGrouped = {};
    treeNodes.map(function (treeNode) {
        if (index < treeNode.phrases.length) {
            if (treeNode.phrases[index].translation in firstPhraseGrouped) {
                firstPhraseGrouped[treeNode.phrases[index].translation] += treeNode.probability;
            } else {
                firstPhraseGrouped[treeNode.phrases[index].translation] = treeNode.probability;
            }
        }
    });

    var tuples = [];
    for (var key in firstPhraseGrouped) {
        tuples.push([key, firstPhraseGrouped[key]]);
    }

    tuples.sort(function(a, b) {
        a = a[1];
        b = b[1];

        return a > b ? -1 : (a < b ? 1 : 0);
    });

    return tuples;
}

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

class Autocompletor {
    constructor(originalPhrase, phrases, autocompleteSize, autocompleteCallback) {
        this.originalPhrase = originalPhrase;
        this.phrases = phrases;
        this.treeRoot = null;
        this.matchedPhrases = [];
        this.lastMatchedWord = 0;
        this.autocompleteList = [];
        this.autocompleteSize = autocompleteSize;
        this.autocompleteCallback = autocompleteCallback;

        this.refresh("")
    }

    updateMatchedPhrases(input) {
        var translatedText = tokenize(input);
        this.matchedPhrases = [];
        this.lastMatchedWord = 0;

        var words = translatedText.split(" ");
        // Try each word except last to match as phrase.
        for (var j = 0; j < words.length - 2; j++) {
            for (var end = words.length - 2; end > j; end--) {
                // from j to end try to match phrase

                var tryToMatch = words.slice(j, end).join(" ");
                //console.log("Trying from " + j + " to " + end + " to match: '" + tryToMatch + "'.");

                for (var k = 0; k < this.phrases.length; k++) {
                    if (this.phrases[k].translation == tryToMatch) {
                        console.log("--Matched '" +  tryToMatch + "' with '" + this.phrases[k].original + "' from server phrase.--");
                        this.matchedPhrases.push(this.phrases[k]);
                        if (this.lastMatchedWord < end) {
                            this.lastMatchedWord = end;
                        }
                        j += (end - j - 1);
                        end = j;
                        break;
                    }
                }
            }
        }

        console.log("Result of matched phrases:");
        console.log(this.matchedPhrases);
        this.refresh(translatedText);
    }

    refresh(input) {
        var grouped;
        var prefixWord = null;
        var originalPhraseLength = this.originalPhrase.split(" ").length;
        this.treeRoot = new TreeNode(this.matchedPhrases, [], []);
        for (var i = 0; i < originalPhraseLength; i++) {
            this.treeRoot.translated.push(false);
        }
        for (var j = 0; j < this.matchedPhrases.length; j++) {
            for (var i = this.matchedPhrases[j].beginIndex; i <= this.matchedPhrases[j].endIndex; i++) {
                this.treeRoot.translated[i] = true;
            }
        }
        var words = input.split(" ");
        if (words[words.length - 1].length > 0) {
            prefixWord = words[words.length - 1];
        }
        this.treeRoot = buildTree(this.phrases, this.treeRoot, prefixWord);

        grouped = groupAndSortTreeNodesByPhraseTranslation(this.matchedPhrases.length, this.treeRoot.getLastStepChildren());

        var filtered = grouped;

        this.autocompleteList = [];
        for(var i = 0; i < this.autocompleteSize && i < filtered.length; i++) {
            var string = detokenize(filtered[i][0]);

            this.autocompleteList.push(string);
        }
        this.autocompleteCallback(this.autocompleteList);
    }

    autocomplete(input, autocompleteIndex) {
        var translatedText = tokenize(input);
        var autocompleteText = this.autocompleteList[autocompleteIndex];

        var start = Math.max(translatedText.length - autocompleteText.length, 0);
        var index = 0;
        var indexSet = false;
        for (; start < translatedText.length; start++) {
            var ok = true;
            for (var i = start; i < translatedText.length; i++) {
                if (autocompleteText[i - start] != translatedText[i]) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                index = start;
                indexSet = true;
                break;
            }
        }

        var out;
        if (indexSet) {
            out = translatedText + autocompleteText.slice(translatedText.length - index, autocompleteText.length);
        } else {
            out = translatedText + autocompleteText;
        }

        this.updateMatchedPhrases(out);
        return detokenize(out);
    }

    getAutocompleteListLength() {
        return this.autocompleteList.length;
    }
}

function tokenize(string) {
    var out = string.toLowerCase();
    var separators = [".", ",", "!", "?", ":", ";", "\"", "\'"];
    for (var i = 0; i < separators.length; i++) {
        out = out.replace(new RegExp("\\" + separators[i], "g"), " " + separators[i] + " ");
    }
    return out.replace(/\s\s+/g, ' ');
}

function detokenize(string) {
    detokenize(string, true);
}

function detokenize(string, capitalize) {
    var out = string.trim();

    out = out.replace(/[ ]*("|')[ ]*/g, "$1");
    out = out.replace(/([^" ])"([^"]*)"([^" ])/g, "$1 \"$2\" $3");
    out = out.replace(/([^' ])'([^']*)'([^' ])/g, "$1 \'$2\' $3");

    out = out.replace(/[ ]*(\.|\!|\?|\;|\,|\:)/g, "$1");

    if (capitalize) {
        out = out.replace(/([ ]*)([^.!?])([^.!?]*[.!?])/g, function (match, g1, g2, g3) {
            return g1 + g2.toUpperCase() + g3;
        });
    }

    return out;
}