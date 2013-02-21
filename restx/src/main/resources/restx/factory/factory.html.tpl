<!DOCTYPE html>
<html>
<head>
    <title>Components Warehouse Content</title>
    <script src="https://raw.github.com/anvaka/VivaGraphJS/99c6cd4fac345b627ea8e7edda923307e898d6b7/dist/vivagraph.min.js"></script>
    <script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
    <script>
        function main () {
            // This demo shows how to create an SVG node which is a bit more complex
            // than single image. Do accomplish this we use 'g' element and 
            // compose group of elements to represent a node.
            var graph = Viva.Graph.graph();


            var searchedQuery, hover = false, defaultOpacity = 0.05, defaultLabelOpacity = 0;

            function applyOpacities() {
                graph.forEachLink(function (link) {
                    link.ui.attr('opacity', (searchedQuery || hover) ? defaultOpacity : 1);
                });
                //noinspection JSValidateTypes
                graph.forEachNode(function (node) {
                    node.ui.attr('opacity', opacityForNode(node));
                    node.ui.children('text')[0].attr('opacity', opacityForNodeLabel(node));
                });
            }

            function highlightSearchedNodes() {
                searchedQuery = $('#search').val();
                applyOpacities();
            };

            function opacityForNode(node) {
                return (searchedQuery &&
                        node && node.data && node.data.name.toUpperCase().indexOf(searchedQuery.toUpperCase()) !== -1)
                        ? 1
                        : ((hover || searchedQuery) ? defaultOpacity : 1);
            }
            function opacityForNodeLabel(node) {
                return (searchedQuery &&
                        node && node.data && node.data.name.toUpperCase().indexOf(searchedQuery.toUpperCase()) !== -1)
                        ? 1 : defaultLabelOpacity;
            }
            $('#search').keyup(highlightSearchedNodes);


            var graphics = Viva.Graph.View.svgGraphics(),
                nodeHeight = 24,
                nodeWidth = 24,

                // we use this method to highlight all realted links and nodes
                // when user hovers mouse over a node:
                highlightRelatedNodes = function(node, isOn) {
                    applyOpacities();
                    var opacity = isOn ? 1 : opacityForNode(node);
                    var labelOpacity = isOn ? 1 : opacityForNodeLabel(node);
                    node.ui.attr('opacity', opacity);
                    node.ui.children('text')[0].attr('opacity', labelOpacity);
                    graph.forEachLinkedNode(node.id, function(node, link){
                       if (link && link.ui) {
                           link.ui.attr('opacity', isOn ? 1 : (searchedQuery ? defaultOpacity : 1));
                           link.ui.attr('stroke', isOn ? 'orange' : 'gray');
                           node.ui.attr('opacity', isOn ? 1 : opacityForNode(node));
                           node.ui.children('text')[0].attr('opacity', isOn ? 1 : opacityForNodeLabel(node));
                       }
                    });
                },
                colors = {
                    'RestxResource': '#4ECDC4',
                    'StringConverter': '#C7F464',
                    'RestxRoute': '#FF6B6B',
                    'RestxRouter': '#C44D58',
                    'default': '#556270'
                };

            var layout = Viva.Graph.Layout.forceDirected(graph, {
                               springLength : 350,
                               gravity : -1
                            });

            // Render the graph
            var renderer = Viva.Graph.View.renderer(graph, {
                    layout     : layout,
                    graphics : graphics,
                    container : $('#graph')[0]
                });
            renderer.run();

            graphics.node(function(node) {
              // This time it's a group of elements: http://www.w3.org/TR/SVG/struct.html#Groups
              var ui = Viva.Graph.svg('g'),
                  svgText = Viva.Graph.svg('text').attr('opacity', defaultLabelOpacity)
                          .attr('y', '-4px').attr('x', (nodeWidth + 4) + 'px').text(node.data ? node.data.name : node.id),
                  img = Viva.Graph.svg('rect')
                     .attr('width', nodeWidth)
                     .attr('height', nodeHeight)
                     .attr('stroke', node.data ? colors[node.data.type] || colors.default : colors.default)
                     .attr('fill', node.data ? colors[node.data.type] || colors.default : colors.default);

              ui.attr('opacity', 1);
              ui.append(svgText);
              ui.append(img);

                $(img).hover(function() { // mouse over
                    hover = true;
                    highlightRelatedNodes(node, true);
                }, function() { // mouse out
                    hover = false;
                    highlightRelatedNodes(node, false);
                });

              ui.width = nodeWidth;
              ui.height = nodeHeight;

              return ui;
            }).placeNode(function(nodeUI, pos) {
                // 'g' element doesn't have convenient (x,y) attributes, instead
                // we have to deal with transforms: http://www.w3.org/TR/SVG/coords.html#SVGGlobalTransformAttribute
                nodeUI.attr('transform',
                            'translate(' +
                                  (pos.x - nodeUI.width/2) + ',' + (pos.y - nodeUI.height/2) +
                            ')');
            });

            // To render an arrow we have to address two problems:
            //  1. Links should start/stop at node's bounding box, not at the node center.
            //  2. Render an arrow shape at the end of the link.

            // Rendering arrow shape is achieved by using SVG markers, part of the SVG
            // standard: http://www.w3.org/TR/SVG/painting.html#Markers
            var createMarker = function(id) {
                    return Viva.Graph.svg('marker')
                               .attr('id', id)
                               .attr('viewBox', "0 0 10 10")
                               .attr('refX', "10")
                               .attr('refY', "5")
                               .attr('markerUnits', "strokeWidth")
                               .attr('markerWidth', "10")
                               .attr('markerHeight', "5")
                               .attr('orient', "auto");
                },

                marker = createMarker('Triangle');
            marker.append('path').attr('d', 'M 0 0 L 10 5 L 0 10 z');

            // Marker should be defined only once in <defs> child element of root <svg> element:
            var defs = graphics.getSvgRoot().append('defs');
            defs.append(marker);

            var geom = Viva.Graph.geom();

            graphics.link(function(link){
                // Notice the Triangle marker-end attribe:
                return Viva.Graph.svg('path')
                           .attr('opacity', 1)
                           .attr('stroke', 'gray')
                           .attr('marker-end', 'url(#Triangle)');
            }).placeLink(function(linkUI, fromPos, toPos) {
                // Here we should take care about
                //  "Links should start/stop at node's bounding box, not at the node center."

                // For rectangular nodes Viva.Graph.geom() provides efficient way to find
                // an intersection point between segment and rectangle
                var toNodeWidth = nodeWidth,
                    toNodeHeight = nodeHeight,
                    fromNodeWidth = nodeWidth,
                    fromNodeHeight = nodeHeight
                    ;

                var from = geom.intersectRect(
                        // rectangle:
                                fromPos.x - fromNodeWidth / 2, // left
                                fromPos.y - fromNodeHeight / 2, // top
                                fromPos.x + fromNodeWidth / 2, // right
                                fromPos.y + fromNodeHeight / 2, // bottom
                        // segment:
                                fromPos.x, fromPos.y, toPos.x, toPos.y)
                           || fromPos; // if no intersection found - return center of the node

                var to = geom.intersectRect(
                        // rectangle:
                                toPos.x - toNodeWidth / 2, // left
                                toPos.y - toNodeHeight / 2, // top
                                toPos.x + toNodeWidth / 2, // right
                                toPos.y + toNodeHeight / 2, // bottom
                        // segment:
                                toPos.x, toPos.y, fromPos.x, fromPos.y)
                            || toPos; // if no intersection found - return center of the node

                var data = 'M' + from.x + ',' + from.y +
                           'L' + to.x + ',' + to.y;

                linkUI.attr("d", data);
            });

            // graph content
{nodes}
{links}

        }
    </script>
    
    <style type="text/css" media="screen">
        html, body, svg { width: 100%; height: 100%;}
    </style>
</head>
<body style="background: #302E30; " onload='main()'>
<div id="panel" style="position: absolute; top: 5px; bottom: 5px; left: 10px; width: 200px; color: #F3F7E4;">
    <div>Search: <input id="search"> </div>
</div>
    <div id="graph" style="position: absolute; top: 5px; bottom: 5px; right: 5px; left: 215px; background: #eeeeee; "></div>
</body>
</html>