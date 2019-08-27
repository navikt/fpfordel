function loadJSON(path, container) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                process(JSON.parse(xhr.responseText), container);
            }
            else {
                console.log("ERROR");
            }
        }
    };
    xhr.open('GET', path, true);
    xhr.send();
}

function process(json, container) {
    var root = json.nodes.find(function(node) {
            return node.id == json.root.id;
    });
    root.color = "lightgray";
    json.nodes.forEach(function(node) {
            node.shadow = "color:'rgb(0,255,0)'";
            node.label = node.ruleDescription;
    });
    var nodes = new vis.DataSet(json.nodes);

    json.edges.forEach(function(edge) {
            edge.arrows = "to";
            edge.from = edge.source;
            edge.to = edge.target;
            if (edge.role == "(IKKE )") {
                edge.role = "";
            }
            edge.label = edge.role;
    });
    var edges = new vis.DataSet(json.edges);

    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {
            edges: {
                    font: {
                            size: 14
                    }
            },
            nodes: {
                    shape: 'box',
                    font: {
                            size: 18
                    },
                    margin: 10,
                    widthConstraint: {
                            maximum: 200
                    }
            },
            layout: {
                randomSeed: undefined,
                improvedLayout:true,
                hierarchical: {
                    enabled:true,
                    levelSeparation: 150,
                    nodeSpacing: 300,
                    treeSpacing: 200,
                    blockShifting: true,
                    edgeMinimization: true,
                    parentCentralization: true,
                    direction: 'UD',        // UD, DU, LR, RL
                    sortMethod: 'directed'   // hubsize, directed
                }
            }
    };
    var network = new vis.Network(container, data, options);

}
