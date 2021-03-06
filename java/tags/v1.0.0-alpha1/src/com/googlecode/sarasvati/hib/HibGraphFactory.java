/*
    This file is part of Sarasvati.

    Sarasvati is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Sarasvati is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Sarasvati.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2008 Paul Lorenz
*/

package com.googlecode.sarasvati.hib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import com.googlecode.sarasvati.Arc;
import com.googlecode.sarasvati.ArcToken;
import com.googlecode.sarasvati.Env;
import com.googlecode.sarasvati.Graph;
import com.googlecode.sarasvati.Node;
import com.googlecode.sarasvati.NodeToken;
import com.googlecode.sarasvati.Process;
import com.googlecode.sarasvati.load.AbstractGraphFactory;
import com.googlecode.sarasvati.load.LoadException;
import com.googlecode.sarasvati.load.NodeFactory;

public class HibGraphFactory extends AbstractGraphFactory<HibGraph>
{
  private Session session;

  HibGraphFactory(Session session)
  {
    super( HibNode.class );
    this.session = session;
  }

  @Override
  public HibGraph newGraph (String name, int version)
  {
    HibGraph newGraph = new HibGraph( name, version );
    session.save( newGraph );
    return newGraph;
  }

  @Override
  public HibArc newArc (HibGraph graph, Node startNode, Node endNode, String name)
  {
    HibArc arc = new HibArc( graph, startNode, endNode, name );
    session.save( arc );
    graph.getArcs().add( arc );
    return arc;
  }

  @Override
  public Node newNode (HibGraph graph, String name, String type, boolean isJoin, boolean isStart, String guard, List<Object> customList)
    throws LoadException
  {
    NodeFactory nodeFactory = getNodeFactory( type );
    HibNode node = (HibNode)nodeFactory.newNode( type );
    node.setGraph( graph );
    node.setName( name );
    node.setType( type );
    node.setJoin( isJoin );
    node.setStart( isStart );
    node.setGuard( guard );

    if ( customList != null )
    {
      for ( Object custom : customList )
      {
        nodeFactory.loadCustom( node, custom );
      }
    }

    node.create( session );

    HibNodeRef nodeRef = new HibNodeRef( graph, node, "" );
    session.save( nodeRef );
    graph.getNodes().add( nodeRef );

    return nodeRef;
  }

  @Override
  public HibArcToken newArcToken (Process process, Arc arc, NodeToken previousToken)
  {
    HibArcToken token = new HibArcToken( (HibProcess)process, (HibArc)arc, (HibNodeToken)previousToken );
    session.save( token );
    return token;
  }

  @Override
  public Node importNode (HibGraph graph, Node node, String instanceName)
  {
    HibNodeRef nodeRef = (HibNodeRef)node;

    String label = nodeRef.getInstance();
    label = label == null || "".equals( label ) ? instanceName : instanceName + ":" + label;

    HibNodeRef newRef = new HibNodeRef( graph, nodeRef.getNode(), label );
    session.save( newRef );
    graph.getNodes().add( newRef );

    return newRef;
  }

  @SuppressWarnings("unchecked")
  @Override
  public HibNodeToken newNodeToken (Process process, Node node, List<ArcToken> parents)
  {
    // Here we setup the token attributes for the new node
    // If the node has no predecessors, it will have no attributes
    // If it has only one processor (or only one processor with attributes)
    // it will inherit the attributes of that one node
    // Otherwise, the attributes of all predecessor nodes will get merged into
    // a single set.
    List<HibArcToken> hibParents = (List<HibArcToken>)(List<?>)parents;

    HibNodeToken attrSetToken = null;
    Map<String,String> attrMap = new HashMap<String,String>();
    Map<String,Object> transientAttributes = new HashMap<String, Object>();
    boolean isMerge = false;

    for ( HibArcToken arcToken : hibParents )
    {
      HibNodeToken parent = arcToken.getParentToken();

      if ( parent.getAttrSetToken() == null )
      {
        continue;
      }
      if ( attrSetToken == null )
      {
        attrSetToken = parent.getAttrSetToken();
      }
      else if ( !isMerge )
      {
        attrMap.putAll( attrSetToken.getAttrMap() );
        isMerge = true;
      }

      if ( isMerge )
      {
        attrMap.putAll( parent.getAttrMap() );
      }

      Env mergeEnv = parent.getEnv();
      for ( String name : mergeEnv.getTransientAttributeNames() )
      {
        transientAttributes.put( name, mergeEnv.getTransientAttribute( name ) );
      }
    }

    HibNodeToken token = new HibNodeToken( (HibProcess)process, (HibNodeRef)node, attrSetToken, attrMap, hibParents, transientAttributes);
    session.save( token );
    return token;
  }

  @Override
  public HibProcess newProcess (Graph graph)
  {
    HibProcess process = new HibProcess( (HibGraph)graph);
    session.save(  process );
    return process;
  }

  @Override
  public Process newNestedProcess (Graph graph, NodeToken parentToken)
  {
    HibProcess process = new HibProcess( (HibGraph)graph);
    process.setParentToken( parentToken );
    session.save(  process );
    return process;
  }
}