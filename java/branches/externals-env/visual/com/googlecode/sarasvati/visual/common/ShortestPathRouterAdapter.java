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

    Copyright 2008-2009 Paul Lorenz
                        Cheong Chung Onn
*/
package com.googlecode.sarasvati.visual.common;

import java.awt.Point;
import java.util.List;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.Path;
import org.eclipse.draw2d.graph.ShortestPathRouter;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import com.googlecode.sarasvati.visual.util.ConvertUtil;

public class ShortestPathRouterAdapter implements Router
{
  protected GraphSceneImpl<?, ?> scene;
  protected ShortestPathRouter router;
  protected boolean dirty = false;

  public ShortestPathRouterAdapter (GraphSceneImpl<?,?> scene, int spacing)
  {
    this.scene = scene;
    this.router = new ShortestPathRouter();
    router.setSpacing( spacing );
  }

  public void setAdjacentLineSpacing (int spacing)
  {
    router.setSpacing( spacing );
  }

  public void addNodeWidget (Widget w)
  {
    w.addDependency( new WidgetListener( w ) );
  }

  public void removeNodeWidget (Widget w)
  {
    WidgetListener widgetListener = null;
    for (Widget.Dependency dependency : w.getDependencies() )
    {
      if ( dependency instanceof WidgetListener )
      {
        widgetListener = (WidgetListener)dependency;
      }
    }

    if ( widgetListener != null )
    {
      w.removeDependency( widgetListener );
      widgetListener.cleanup();
    }
  }

  @SuppressWarnings("unchecked")
  public void addPath (Path path, boolean isSelfArc)
  {
    //Check for overlapping path, If there are overlapping paths
    //Force path to bend.
    List<Path> solve = router.solve();

    for ( Path existPath : solve )
    {
      if ( (existPath.getPoints() == null || existPath.getPoints().size() == 2 ) &&
           existPath.getEndPoint().equals( path.getStartPoint() ) &&
           existPath.getStartPoint().equals( path.getEndPoint() ) )
      {
        PointList bendPoints = new PointList();
        int deltaX = path.getEndPoint().x - path.getStartPoint().x;
        int y = ((path.getEndPoint().y + path.getStartPoint().y) >> 1 ) + Math.min( Math.abs( deltaX ), router.getSpacing() );

        int deltaY = path.getEndPoint().y - path.getStartPoint().y;
        int offset = Math.min( Math.abs( deltaY ), router.getSpacing() );
        if ( ( deltaX > 0 && deltaY > 0 ) || (deltaX < 0 && deltaY < 0 ) )
        {
          offset = -offset;
        }

        int x = ((path.getEndPoint().x + path.getStartPoint().x) >> 1 ) + offset;

        bendPoints.addPoint( x, y );
        //Add a new obstacle
        path.setBendPoints( bendPoints  );
      }
    }

    if ( isSelfArc )
    {
      int offset = 15;
      PointList bendPoints = new PointList();
      bendPoints.addPoint( path.getStartPoint().x, path.getStartPoint().y + offset );
      bendPoints.addPoint( path.getEndPoint().x - offset, path.getStartPoint().y + offset );
      bendPoints.addPoint( path.getEndPoint().x - offset, path.getEndPoint().y );
      path.setBendPoints( bendPoints );
    }

    router.addPath( path );
    dirty = true;
  }

  public void removePath (Path path)
  {
    router.removePath( path );
    dirty = true;
  }

  @Override
  public List<Point> routeConnection (ConnectionWidget conn)
  {
    PathTrackingConnectionWidget pathTrackingCW = (PathTrackingConnectionWidget)conn;
    pathTrackingCW.ensurePathCurrent();
    router.solve();
    dirty = false;

    return pathTrackingCW.getRoute();
  }

  public void setDirty ()
  {
    this.dirty = true;
  }

  public class WidgetListener implements Widget.Dependency
  {
    private Widget widget;
    private Rectangle bounds;

    public Rectangle getNewBounds ()
    {
      java.awt.Rectangle newBounds = widget.getBounds();
      if (newBounds == null )
      {
        return null;
      }
      newBounds.setLocation( widget.getLocation() );
      return ConvertUtil.awtToSwt( newBounds );
    }

    public WidgetListener (Widget widget)
    {
      this.widget = widget;
      this.bounds = getNewBounds();

      if ( bounds != null )
      {
        router.addObstacle( bounds );
      }

      dirty = true;
    }

    @Override
    public void revalidateDependency ()
    {
      Rectangle newBounds = getNewBounds();

      if ( bounds != null )
      {
        router.removeObstacle( bounds );
      }
      if ( newBounds != null )
      {
        router.addObstacle( newBounds );
      }

      bounds = newBounds;
      dirty = true;
    }

    public void cleanup ()
    {
      if ( bounds != null )
      {
        router.removeObstacle( bounds );
        dirty = true;
      }
    }
  }
}