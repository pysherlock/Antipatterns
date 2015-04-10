/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//gets panel prototype from the panel type list
function getPanelPrototype(aName){
    for (var i = 0; i < Firebug.panelTypes.length; i++) {
        if (aName == Firebug.panelTypes[i].prototype.name) {
            return Firebug.panelTypes[i].prototype;
        }
    }
	
    return null;
}

FBL.ns(function(){
    with (FBL) {
    
        // --------------
        // Globals
        // --------------
        
        var panelTab = null;
        var text = "";
        var http = null;
        var netListener = null;
        var prefDomain = "extensions.firebug.cocoon3profiling";
        var xmlTree = null;
        var argTree = null;
        var doc = null;
        var treeId = 0;
        var idcounter = 0;
        var profilingURL = null;
        
        // --------------
        // Working functions
        // --------------
        
        function onShowPicturePress(){
            if (profilingURL != null) {
                var url = profilingURL + ".png";
                gBrowser.selectedTab = gBrowser.addTab(url);
            }
        }
        
        function onShowXMLPress(){
            if (profilingURL != null) {
                gBrowser.selectedTab = gBrowser.addTab(profilingURL);
            }
        }
        
        function Reload(){
        
            clearPanel();
            netListener.setLoading();
            
            http = getHTTPObject();
            http.onreadystatechange = processResponse;
            
            var requrl = profilingURL + "?sitemap=" + Firebug.getPref(prefDomain, "sitemapenabled");
            requrl = requrl + "&matcher=" + Firebug.getPref(prefDomain, "showmatcher");
            
            http.open("GET", requrl, true);
            http.send(null);
        }
        
        function toggleContainer(treeNode, conName, value){
        
            if (treeNode != null) {
                var children = treeNode.childNodes;
                
                // these are xul-treeitems
                for (var i = 0; i < children.length; i++) {
                
                    var child = children[i];
                    if (child.firstChild.firstChild.getAttribute("label") == conName) {
                        if (child.hasAttribute("container")) {
                            child.setAttribute("open", value);
                        }
                    }
                    else {
                        // get its xul-treechildren
                        if (child.childNodes.length > 1) {
                            toggleContainer(child.childNodes[1], conName, value);
                        }
                    }
                }
            }
        }
        
        function onSelectTree(){
        
            if (xmlTree != null) {
            
                // clear the tree
                while (argTree.lastChild.hasChildNodes()) {
                    argTree.lastChild.removeChild(argTree.lastChild.firstChild);
                }
                
                idcounter = 0;
                var elem = searchSelectedTreeItem(xmlTree.lastChild, xmlTree.currentIndex);
                
                // treeitem element
                if (elem.hasChildNodes()) {
                    var j = 0;
                    var enum1 = ["arguments", "properties"];
                    var key = [];
                    var value = [];
                    var pic = [];
                    
                    for (var z = 0; z < enum1.length; z++) {
                    
                        var child = getElementByName(elem, enum[z]);
                        
                        if (child != null) {
                        
                            // childNodes[1] should be xul-treechildren (see processDOM)
                            // childNodes is a list of all xul-treeitems beneath xul-treechildren
                            if (child.childNodes.length > 1) {
                                var arg_childs = child.childNodes[1].childNodes;
                                for (var t = 0; t < arg_childs.length; t++) {
                                
                                    // firstChild is the xul-treerow, which is located in EVERY xul-treeitem
                                    // childNodes[1] is the second cell here, we can't use "lastChild" because there is a hidden column at the end (see processDOM)
                                    key[j] = arg_childs[t].firstChild.childNodes[1].getAttribute("label");
                                    value[j] = "";
                                    pic[j] = arg_childs[t].firstChild.childNodes[0].getAttribute("label");
                                    // because of the fact, that text is another "subelement", we need to go down another step.
                                    if (arg_childs[t].hasChildNodes()) {
                                        // childNodes[1] is the xul-treechildren element again
                                        if (arg_childs[t].childNodes.length > 1) {
                                            var text_childs = arg_childs[t].childNodes[1].childNodes;
                                            // and again we go for the second xul-treecell with childNodes[1]
                                            value[j] = text_childs[0].firstChild.childNodes[1].getAttribute("label");
                                        }
                                    }
                                    j++;
                                }
                            }
                        }
                    }
                    var result = getElementByName(elem, "result");
                    if (result != null) {
                        // childNodes[1] should be xul-treechildren (see processDOM)
                        // childNodes is a list of all xul-treeitems beneath xul-treechildren
                        var arg_childs = result.childNodes[1].childNodes[0];
                        if (arg_childs != null && arg_childs.hasChildNodes()) {
                        
                            // firstChild is the xul-treerow, which is located in EVERY xul-treeitem
                            // childNodes[1] is the second cell here, we can't use "lastChild" because there is a hidden column at the end (see processDOM)
                            key[j] = arg_childs.firstChild.childNodes[1].getAttribute("label");
                            pic[j] = arg_childs.firstChild.childNodes[0].getAttribute("label");
                            value[j] = "";
                            // childNodes[1] is the xul-treechildren element again
                            
                            var text_childs = arg_childs.childNodes[1].childNodes[0].childNodes[1].childNodes[0];
                            // and again we go for the second xul-treecell with childNodes[1]
                            value[j] = text_childs.firstChild.childNodes[1].getAttribute("label");
                            j++;
                        }
                    }
                    
                    var profiler = getElementByName(elem, "profiler");
                    if (profiler != null) {
                        key[j] = "profiler";
                        pic[j] = "profiler";
                        value[j] = profiler.firstChild.childNodes[1].getAttribute("label");
                    }
                    
                    
                    // do the treefilling!
                    for (var j = 0; j < key.length; j++) {
                    
                        var treeitem = doc.createElement("treeitem");
                        var treerow = doc.createElement("treerow");
                        var keycell = doc.createElement("treecell");
                        var valuecell = doc.createElement("treecell");
                        var hidecell = doc.createElement("treecell");
                        
                        keycell.setAttribute("label", key[j]);
                        keycell.setAttribute("properties", "argtree_" + pic[j]);
                        valuecell.setAttribute("label", value[j]);
                        hidecell.setAttribute("hidden", "true");
                        
                        treerow.appendChild(keycell);
                        treerow.appendChild(valuecell);
                        treerow.appendChild(hidecell);
                        
                        treeitem.appendChild(treerow);
                        
                        // lastChild should be the xul-treechildren
                        argTree.lastChild.appendChild(treeitem);
                    }
                }
            }
        }
        
        // we need this function, because tree.currentIndex just brings us the index of all VISIBLE items.
        // this means, if we have a tree containing 10 items where 3 are not visible because of a closed container
        // the item directly behind the container has the index N. If we open up the container and ask for the index
        // of the very same item, we get N+3.
        // so tree.currentIndex just tell us, what the user sees, and this information is not really usable for our tree.
        // we basicly rebuilt this behaviour to find out which item is used.
        // we skip all closed or hidden items and iterate to the point where the provided index and our personal index is equal.
        // --> yeah, item found!
        
        function searchSelectedTreeItem(root, treeindex){
        
            if (root.hasChildNodes()) {
            
                var children = root.childNodes;
                for (var i = 0; i < children.length; i++) {
                
                    if ((!children[i].getAttribute("hidden")) || (children[i].getAttribute("hidden") == "false")) {
                    
                        if (treeindex == idcounter) {
                        
                            return children[i];
                        }
                        idcounter++;
                        
                        if (children[i].getAttribute("container") == "true") {
                        
                            if (children[i].getAttribute("open") == "true") {
                            
                                var result = searchSelectedTreeItem(children[i].lastChild, treeindex);
                                if (result != null) {
                                
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        // root is a xul-treeitem element,
        // searchname is the name aka label of the namecell (first of three xul-treecell) to search for.
        // we return the treeitem, containing the searched namecell or null
        function getElementByName(root, searchName){
        
            // childNodes[1] is xul-treechildren, childNodes result in further xul-treeitems
            if (root.childNodes[1] != null) {
                var children = root.childNodes[1].childNodes;
                
                for (var i = 0; i < children.length; i++) {
                
                    // children[i] is a xul-treeitem
                    // firstChild would mean the containing xul-treerow
                    // another firstChild is the first of three xul-treecell
                    if (children[i].firstChild.firstChild.getAttribute("label") == searchName) {
                        if (children[i].hasChildNodes()) {
                            return children[i];
                        }
                        else {
                            // if we find it, but it's empty, that is as evil as if it wouldn't exist
                            return null;
                        }
                    }
                }
            }
            return null;
        }
        
        function buildBaseTree(){
        
            clearPanel();
            var pictureButton = this.document.createElement("button");
            pictureButton.setAttribute("label", "Show Picture");
            pictureButton.addEventListener("command", onShowPicturePress, true);
            
            var xmlButton = this.document.createElement("button");
            xmlButton.setAttribute("label", "Show XML");
            xmlButton.addEventListener("command", onShowXMLPress, true);
            
            doc = this.document;
            
            var tree = this.document.createElement("tree");
            var treecols = this.document.createElement("treecols");
            var treecol = this.document.createElement("treecol");
            var attrcol = this.document.createElement("treecol");
            var hidecol = this.document.createElement("treecol");
            var treechildren = this.document.createElement("treechildren");
            var splitter1 = this.document.createElement("splitter");
            var splitter2 = this.document.createElement("splitter");
            
            splitter1.setAttribute("class", "tree-splitter");
            splitter1.setAttribute("resizeafter", "grow");
            splitter2.setAttribute("class", "tree-splitter");
            splitter2.setAttribute("resizeafter", "grow");
            
            hidecol.setAttribute("hidden", "true");
            hidecol.setAttribute("label", "");
            hidecol.setAttribute("ignoreincolumnpicker", "true");
            
            treecol.setAttribute("id", "profilingtree-col1");
            treecol.setAttribute("label", "Name");
            treecol.setAttribute("primary", "true");
            treecol.setAttribute("persist", "width ordinal hidden");
            
            attrcol.setAttribute("id", "profilingtree-col2");
            attrcol.setAttribute("label", "Execution Time");
            attrcol.setAttribute("persist", "width ordinal hidden");
            
            treecols.appendChild(treecol);
            treecols.appendChild(splitter1);
            treecols.appendChild(attrcol);
            treecols.appendChild(splitter2);
            treecols.appendChild(hidecol);
            
            tree.appendChild(treecols);
            
            treechildren.setAttribute("id", "rootchildren");
            tree.appendChild(treechildren);
            
            tree.setAttribute("flex", 1);
            tree.setAttribute("hidecolumnpicker", "true");
            tree.setAttribute("id", "profilingtree");
            tree.setAttribute("seltype", "single");
            tree.addEventListener('select', onSelectTree, true);
            
            xmlTree = tree;
            
            var div = this.document.createElement("div");
            
            div.appendChild(tree);
            
            var tree2 = this.document.createElement("tree");
            var treecols2 = this.document.createElement("treecols");
            var treecol2 = this.document.createElement("treecol");
            var attrcol2 = this.document.createElement("treecol");
            var hidecol2 = this.document.createElement("treecol");
            var treechildren2 = this.document.createElement("treechildren");
            var splitter3 = this.document.createElement("splitter");
            var splitter4 = this.document.createElement("splitter");
            
            treecol2.setAttribute("id", "argumenttree-col1");
            treecol2.setAttribute("label", "Class");
            treecol2.setAttribute("primary", "true");
            treecol2.setAttribute("persist", "width ordinal hidden");
            
            attrcol2.setAttribute("id", "argumenttree-col2");
            attrcol2.setAttribute("label", "Value");
            attrcol2.setAttribute("persist", "width ordinal hidden");
            
            hidecol2.setAttribute("hidden", "true");
            hidecol2.setAttribute("label", "");
            hidecol2.setAttribute("ignoreincolumnpicker", "true");
            
            splitter3.setAttribute("class", "tree-splitter");
            splitter3.setAttribute("resizeafter", "grow");
            splitter4.setAttribute("class", "tree-splitter");
            splitter4.setAttribute("resizeafter", "grow");
            
            treecols2.appendChild(treecol2);
            treecols2.appendChild(splitter3);
            treecols2.appendChild(attrcol2);
            treecols2.appendChild(splitter4);
            treecols2.appendChild(hidecol2);
            
            treechildren2.setAttribute("id", "rootchildren2");
            
            tree2.appendChild(treecols2);
            tree2.appendChild(treechildren2);
            
            tree2.setAttribute("flex", 1);
            tree2.setAttribute("hidecolumnpicker", "true");
            tree2.setAttribute("id", "argumenttree");
            
            argTree = tree2;
            
            div.appendChild(tree2);
            
            div.setAttribute("style", "min-width:100%");
            
            panelTab.appendChild(xmlButton);
            panelTab.appendChild(pictureButton);
            panelTab.appendChild(div);
        }
        
        function processResponse(){
            if (http.readyState == 4) {
                if (http.status == 200) {
                
                    var doc = http.responseXML;
                    buildBaseTree();
                    
                    treeId = 0;
                    processDOM(doc.documentElement, xmlTree.lastChild);
                }
                else {
                    printInfoMessage("The profiling request responsed with statuscode " + http.status + "!");
                }
            }
        }
        
        function processDOM(node, tree){
        
            var treeitem = this.document.createElement("treeitem");
            var treerow = this.document.createElement("treerow");
            var namecell = this.document.createElement("treecell");
            var attrcell = this.document.createElement("treecell");
            var hidecell = this.document.createElement("treecell");
            
            if (node.nodeType == Node.ELEMENT_NODE) {
            
                namecell.setAttribute("properties", node.nodeName);
                namecell.setAttribute("label", node.nodeName);
                if (node.hasAttributes()) {
                
                    var text = "";
                    var attr = node.attributes;
                    //    if the node has an attribute called "name", the name of the namecell will be changed to the value of this attribute
                    for (var i = 0; i < attr.length; i++) {
                        if (attr[i].name == "name" || attr[i].name == "method") {
                            namecell.setAttribute("label", attr[i].value);
                            if (attr[i].value == "Sitemap") {
                                namecell.setAttribute("properties", "sitemap");
                            }
                        }
                        else {
                        
                            if (attr[i].name == "executionTime") {
                                attrcell.setAttribute("label", attr[i].value);
                            }
                            else 
                                if (attr[i].name == "id") {
                                // ignore
                                }
                                else {
                                    attrcell.setAttribute("label", attrcell.getAttribute("label") + attr[i].name + "=\"" + attr[i].value + "\" ");
                                }
                        }
                    }
                    
                }
                else {
                    attrcell.setAttribute("label", "");
                }
                treerow.appendChild(namecell);
                treerow.appendChild(attrcell);
                treerow.appendChild(hidecell);
                treeitem.appendChild(treerow);
                treeitem.setAttribute("id", treeId);
                treeId++;
                if (node.nodeName == "arguments" || node.nodeName == "result" || node.nodeName == "properties" || node.nodeName == "profiler") {
                    treeitem.setAttribute("hidden", "true");
                }
                if (node.hasChildNodes()) {
                
                    treeitem.setAttribute("container", "true");
                    treeitem.setAttribute("open", "true");
                    
                    if ((node.nodeName == "invocation") || (node.nodeName == "arguments")) {
                        treeitem.setAttribute("open", "false");
                        treeitem.setAttribute("container", "false");
                    }
                    if (node.nodeName == "invocations") {
                        treeitem.setAttribute("open", Firebug.getPref(prefDomain, "openinvocations"));
                    }
                    
                    var treechildren = this.document.createElement("treechildren");
                    
                    var childs = node.childNodes;
                    for (var i = 0; i < childs.length; i++) {
                        processDOM(childs[i], treechildren);
                    }
                    treeitem.appendChild(treechildren);
                }
                
                tree.appendChild(treeitem);
            }
            else 
                if (node.nodeType == Node.TEXT_NODE || node.nodeType == Node.CDATA_SECTION_NODE) {
                    if (!is_all_ws(node.textContent)) {
                        namecell.setAttribute("label", "text");
                        attrcell.setAttribute("label", node.textContent);
                        attrcell.setAttribute("properties", "text");
                        treerow.appendChild(namecell);
                        treerow.appendChild(attrcell);
                        treeitem.appendChild(treerow);
                        tree.appendChild(treeitem);
                    }
                }
        }
        
        // --------------
        // Help functions
        // --------------
        
        function getHTTPObject(){
            if (typeof XMLHttpRequest != 'undefined') {
                return new XMLHttpRequest();
            }
            
            try {
                return new ActiveXObject("Msxml2.XMLHTTP");
            } 
            catch (e) {
                try {
                    return new ActiveXObject("Microsoft.XMLHTTP");
                } 
                catch (e) {
                }
            }
            return false;
        }
        
        
        function is_all_ws(nod){
            // Use ECMA-262 Edition 3 String and RegExp features
            return !(/[^\t\n\r ]/.test(nod));
        }
        
        function printInfoMessage(message){
            var para = this.document.createElement("label");
            para.style.color = "red";
            para.setAttribute("value", message);
            panelTab.appendChild(para);
        }
        
        function printPicture(url){
            var img = this.document.createElement("image");
            img.setAttribute("src", url);
            panelTab.appendChild(img);
        }
        
        function clearPanel(){
            if (panelTab != null) {
                while (panelTab.firstChild != null) {
                    panelTab.removeChild(panelTab.firstChild);
                }
            }
        }
        
        // ------------------
        // "Objects"
        // ------------------
        
        Firebug.Cocoon3ProfilingModule = extend(Firebug.Module, {
            originalPanelGetMenuFunction: null,
            
            initialize: function(owner){
                Firebug.Module.initialize.apply(this, arguments);
                // Create and Register NetMonitor listener
                netListener = new NetListener();
                Firebug.NetMonitor.NetInfoBody.addListener(netListener);
                
                
                var panel = getPanelPrototype("net");
                this.originalPanelGetMenuFunction = panel.getOptionsMenuItems;
                panel.getOptionsMenuItems = bindFixed(this.overrideGetOptionsMenu, this);
            },
            
            shutdown: function(){
                Firebug.Module.shutdown.apply(this, arguments);
                
                // Unregister NetMonitor listener
                Firebug.NetMonitor.NetInfoBody.removeListener(netListener);
            },
            
            overrideGetOptionsMenu: function(){
                var result = this.originalPanelGetMenuFunction();
                
                var value = Firebug.getPref(prefDomain, "sitemapenabled");
                var invValue = Firebug.getPref(prefDomain, "openinvocations");
                var matchValue = Firebug.getPref(prefDomain, "showmatcher");
                //using bindFixed from Firebug
                //first item is definition of Enable history menu item in options menu
                //second item is definition of Set history depth menu item in options menu
                result.push({
                    label: "(c3p) Show Sitemap",
                    nol10n: false,
                    type: "checkbox",
                    checked: value,
                    command: bindFixed(this.setEnabledPreference, this, !value)
                }, {
                    label: "(c3p) Open Invocations",
                    nol10n: false,
                    type: "checkbox",
                    checked: invValue,
                    command: bindFixed(this.setInvPreference, this, !invValue)
                }, {
                    label: "(c3p) Show Unmached Matchers",
                    nol10n: false,
                    type: "checkbox",
                    checked: matchValue,
                    command: bindFixed(this.setMatcherPreference, this, !matchValue)
                });
                
                return result;
            },
            
            setEnabledPreference: function(newvalue){
                var ret = Firebug.setPref(prefDomain, "sitemapenabled", newvalue);
                Reload();
                return ret;
            },
            
            setInvPreference: function(newvalue){
                // xmlTree.lastChild is the xul-treechildren element
                toggleContainer(xmlTree.lastChild, "invocations", newvalue);
                return Firebug.setPref(prefDomain, "openinvocations", newvalue);
            },
            
            setMatcherPreference: function(newvalue){
                var ret = Firebug.setPref(prefDomain, "showmatcher", newvalue);
                Reload();
                return ret;
            },
        });
        
        
        // working object
        function NetListener(){
        }
        NetListener.prototype = {
            // Listener for NetInfoBody.
            initTabBody: function(infoBox, file){
                Firebug.NetMonitor.NetInfoBody.appendTab(infoBox, "CocoonProfiling", "Cocoon3 Profiling");
                this.addStyleSheet(infoBox.ownerDocument);
            },
            destroyTabBody: function(infoBox, file){
                // nothing to do till now, perhaps remove CSS and such
            },
            updateTabBody: function(infoBox, file, context){
            
                // Get currently selected tab.
                var tab = infoBox.selectedTab;
                
                // Generate content only for the first time; and only if our tab
                // has been just activated.
                if (tab.dataPresented || !hasClass(tab, "netInfoCocoonProfilingTab")) 
                    return;
                
                // Make sure the content is generated just once.
                tab.dataPresented = true;
                
                // Get body element associated with the tab.
                var tabBody = getElementByClass(infoBox, "netInfoCocoonProfilingText");
                
                var headers = file.responseHeaders;
                var id = null;
                var url = null;
                if (headers) {
                    for (var i = 0; i < headers.length; ++i) {
                        var header = headers[i];
                        if (header.name == "X-Cocoon-Profiling-ID") {
                            id = header.value;
                        }
                        if (header.name == "X-Cocoon-Profiling-URL") {
                            url = header.value;
                        }
                    }
                }
                
                panelTab = tabBody;
                if (id != null || url != null) {
                
                    this.setLoading();
                    
                    http = getHTTPObject();
                    http.onreadystatechange = processResponse;
                    
                    if (url == null) {
                        var requrl = "http://localhost:8888/controller/profiling/" + id;
                    }
                    else {
                        var index = file.href.lastIndexOf("/") + 1;
                        var requrl = file.href.substring(0, index) + url;
                    }
                    
                    profilingURL = requrl;
                    requrl = requrl + "?sitemap=" + Firebug.getPref(prefDomain, "sitemapenabled");
                    requrl = requrl + "&matcher=" + Firebug.getPref(prefDomain, "showmatcher");
                    
                    http.open("GET", requrl, true);
                    http.send(null);
                }
                else {
                    printInfoMessage("No profiling data available for this request!");
                }
            },
            
            addStyleSheet: function(doc){
                // Make sure the stylesheet isn't appended twice.
                if ($("cocoonprofilingStyles", doc)) 
                    return;
                
                var styleSheet = createStyleSheet(doc, "chrome://cocoon3profiling/skin/cocoon3profiling.css");
                styleSheet.setAttribute("id", "cocoonprofilingStyles");
                addStyleSheet(doc, styleSheet);
            },
            
            setLoading: function(){
                printPicture("chrome://cocoon3profiling/skin/loading.gif");
                printInfoMessage(" Loading...");
            },
        };
        
        Firebug.registerModule(Firebug.Cocoon3ProfilingModule);
    }
});

