/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rafkind.masterofmagic.ui.swing

import javax.swing.JPanel;
import java.awt._;
import java.awt.event._;
import java.awt.geom._;

import com.rafkind.masterofmagic.state._;
import com.rafkind.masterofmagic.util._;

class MapPanel(overworld:Overworld, imageLibrarian:ImageLibrarian) extends JPanel{

  // size of the big viewport, in tiles
  val VIEW_WIDTH = 12;
  val VIEW_HEIGHT = 10;

  var transform = new AffineTransform();
  setDoubleBuffered(true);

  addComponentListener(new ComponentAdapter {
      override def componentResized(e:ComponentEvent):Unit = {
        val w1:Double = VIEW_WIDTH * TerrainLbxReader.TILE_WIDTH;
        val h1:Double = VIEW_HEIGHT * TerrainLbxReader.TILE_HEIGHT;

        val w2:Double = e.getComponent().getWidth();
        val h2:Double = e.getComponent().getHeight();

        transform.setToScale(w2/w1, h2/h1);
      }
  });

  addMouseListener(new MouseAdapter {
      override def mouseClicked(e:MouseEvent):Unit = {
        val whereClicked = componentToMapCoordinates(e.getPoint());

        windowStartX = whereClicked.x - VIEW_WIDTH / 2;
        if (windowStartX < 0) {
          windowStartX += overworld.width;
        }
        if (windowStartX > overworld.width) {
          windowStartX -= overworld.width;
        }
        
        windowStartY = whereClicked.y - VIEW_HEIGHT / 2;
        if (windowStartY < 0) {
          windowStartY = 0;
        }
        if (windowStartY + VIEW_HEIGHT >= overworld.height) {
          windowStartY = overworld.height - VIEW_HEIGHT;
        }
        repaint();
      }
  });

  var windowStartX = 0;
  var windowStartY = 0;
  var currentPlane = Plane.ARCANUS;

  def componentToMapCoordinates(p:Point2D):Point = {
    val q = new Point;
    transform.inverseTransform(p, q);
    val x = q.x / TerrainLbxReader.TILE_WIDTH;
    val y = q.y / TerrainLbxReader.TILE_HEIGHT;
    val answer = new Point(x + windowStartX,y + windowStartY);    
    answer;
  }
  
  override def paintComponent(g:Graphics):Unit = {
    val g2d = g.asInstanceOf[Graphics2D];

    val saveTransform = g2d.getTransform();
    g2d.transform(transform);
    
    for (j <- 0 until VIEW_HEIGHT) {
      for (i <- 0 until VIEW_WIDTH) {
        val ix = i * TerrainLbxReader.TILE_WIDTH;
        val iy = j * TerrainLbxReader.TILE_HEIGHT;

        val t = overworld.get(currentPlane, i + windowStartX, j + windowStartY);
        g.drawImage(
          imageLibrarian.getTerrainTileImage(t),
          ix,
          iy,
          null);

        t.place match {
          case Some(place)  =>
            place match {
              case lair:Lair =>
                g.drawImage(
                  imageLibrarian.getLairTileImage(lair.lairType),
                  ix,
                  iy,
                  null);
              case city:City =>
                g.drawImage(
                  imageLibrarian.getCityTileImage(city),
                  ix,
                  iy,
                  null);
              case _ =>
            }
          case _ =>
        }

        t.armyUnitStack match {
          case Some(armyUnitStack) =>
            g.drawImage(
              imageLibrarian.getArmyUnitTileImage(armyUnitStack.units(0)),
              ix, iy, null
            );
          case _ =>
        }
      }
    }

    g2d.setTransform(saveTransform);
  }
}