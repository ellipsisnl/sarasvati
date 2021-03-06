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
package com.googlecode.sarasvati;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

public class TestNodeToken extends TestToken<NodeToken>
{
  protected List<TestArcToken> parents  = new LinkedList<TestArcToken>();
  protected List<TestArcToken> children = new LinkedList<TestArcToken>();

  protected String             id;
  protected Node               node;

  public TestNodeToken (int lineNumber, String id, Node node, boolean complete, ExecutionType executionType)
  {
    super( lineNumber, complete, executionType );
    this.lineNumber = lineNumber;
    this.id = id;
    this.node = node;
  }

  public String getId ()
  {
    return id;
  }

  public void setId (String id)
  {
    this.id = id;
  }

  public Node getNode ()
  {
    return node;
  }

  public void setNode (Node node)
  {
    this.node = node;
  }

  public List<TestArcToken> getParents ()
  {
    return parents;
  }

  public void setParents (List<TestArcToken> parents)
  {
    this.parents = parents;
  }

  public List<TestArcToken> getChildren ()
  {
    return children;
  }

  public void setChildren (List<TestArcToken> children)
  {
    this.children = children;
  }

  public void addChild (TestArcToken childToken)
  {
    children.add( childToken );
  }

  public void addParent (TestArcToken parentToken)
  {
    parents.add( parentToken );
  }

  @Override
  public void validate ()
  {
    Assert.assertEquals( "Node does not match on " + toString(), node, token.getNode() );
    super.validate();
  }

  @Override
  public String toString ()
  {
    return "[TestNodeToken id=" + id + " line=" + lineNumber + "]";
  }
}