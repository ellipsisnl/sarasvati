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
package com.googlecode.sarasvati.editor.command;

import java.awt.Point;

import org.netbeans.api.visual.widget.Widget;

import com.googlecode.sarasvati.editor.model.EditorGraphMember;
import com.googlecode.sarasvati.editor.model.EditorScene;

public class MoveNodeCommand implements Command
{
  private EditorScene scene;
  private EditorGraphMember member;
  private Point startLocation;
  private Point endLocation;

  public MoveNodeCommand (EditorScene scene, EditorGraphMember member, Point startLocation, Point endLocation)
  {
    this.scene = scene;
    this.member = member;
    this.startLocation = startLocation;
    this.endLocation = endLocation;
  }

  @Override
  public void performAction ()
  {
    member.setOrigin( endLocation );
    Widget widget = scene.findWidget( member );
    widget.setPreferredLocation( endLocation );
    widget.revalidate();
    widget.getScene().validate();
  }

  @Override
  public void undoAction ()
  {
    member.setOrigin( startLocation );
    Widget widget = scene.findWidget( member );
    widget.setPreferredLocation( startLocation );
    widget.revalidate();
    widget.getScene().validate();
  }

  @Override
  public String getName ()
  {
    return "Move";
  }
}