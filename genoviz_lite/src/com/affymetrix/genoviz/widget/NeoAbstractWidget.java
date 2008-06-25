/**
*   Copyright (c) 1998-2008 Affymetrix, Inc.
*
*   Licensed under the Common Public License, Version 1.0 (the "License").
*   A copy of the license must be included with any distribution of
*   this source code.
*
*   The license is also available at
*   http://www.opensource.org/licenses/cpl.php
*/

package com.affymetrix.genoviz.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import com.affymetrix.genoviz.bioviews.GlyphI;

import com.affymetrix.genoviz.bioviews.TransformI;
import com.affymetrix.genoviz.util.GeneralUtils;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides basic functionallity for all genoviz Widgets.
 */
public abstract class NeoAbstractWidget extends Container
  implements MouseListener, MouseMotionListener, KeyListener {

  protected Dimension pref_widg_size = new Dimension(1, 1);

  protected List<MouseListener> mouse_listeners = new CopyOnWriteArrayList<MouseListener>();
  protected List<MouseMotionListener> mouse_motion_listeners = new CopyOnWriteArrayList<MouseMotionListener>();
  protected List<KeyListener> key_listeners = new CopyOnWriteArrayList<KeyListener>();

  protected Hashtable<GlyphI,Object> glyph_hash = new Hashtable<GlyphI,Object>();

  //TODO: This should maybe be Map<Object,List<GlyphI>> // xxx
  protected Hashtable<Object,Object> model_hash = new Hashtable<Object,Object>();

  protected boolean models_have_multiple_glyphs = false;

  protected static Hashtable<String,Color> colormap = GeneralUtils.getColorMap();

  protected int scroll_behavior[] = new int[2];

  protected List<GlyphI> selected = new ArrayList<GlyphI>();

  /**
   *  whether any models are represented by multiple glyphs
   *  WARNING: once one model is represented by multiple glyphs, this flag will only
   *     be reset to false when clearWidget() is called
  */
  public boolean hasMultiGlyphsPerModel() {
    return models_have_multiple_glyphs;
  }

  @SuppressWarnings("unchecked")
  public void setDataModel(GlyphI gl, Object datamodel) {
    // glyph to datamodel must be one-to-one
    // datamodel to glyph can be one-to-many

    glyph_hash.put(gl, datamodel);
    gl.setInfo(datamodel);

    // more than one glyph may be associated with the same datamodel!
    // therefore check and see if already a glyph associated with this datamodel
    // if so, create a new list and add glyphs to it (or extend the pre-exisiting one)
    Object previous = model_hash.get(datamodel);
    if (previous == null) {
      model_hash.put(datamodel, gl);
    }
    else {
      models_have_multiple_glyphs = true;
      if (previous instanceof List) {
        ((List<GlyphI>) previous).add(gl);
      }
      else {
        ArrayList<GlyphI> glyphs = new ArrayList<GlyphI>();
        glyphs.add((GlyphI) previous);
        glyphs.add(gl);
        model_hash.put(datamodel, glyphs);
      }
    }
  }

  public Object getDataModel(GlyphI glyph) {
    return glyph.getInfo();
  }

  /**
   *  This should be static, except interface methods can't be static
   */
  public void addColor(String name, Color col) {
    if (null == name) {
      throw new IllegalArgumentException("can't addColor without a name.");
    }
    if (null == col) {
      throw new IllegalArgumentException("can't add a null color.");
    }
    colormap.put(name, col);
  }

  public Color getColor(String name) {
    if (null == name) {
      throw new IllegalArgumentException("can't getColor without a name.");
    }
    return colormap.get(name);
  }
  public String getColorName(Color theColor) {
    if (null == theColor) {
      throw new IllegalArgumentException("can't get a name for a null color.");
    }
    Enumeration it = colormap.keys();
    while (it.hasMoreElements()) {
      String candidate = (String)it.nextElement();
      if (theColor.equals(colormap.get(candidate))) {
        return candidate;
      }
    }
    return null;
  }
  public Enumeration getColorNames() {
    return colormap.keys();
  }


  /**
   * Gets the visibility of an item in the widget.
   *
   * @param gl the item in question.
   */
  public boolean getVisibility(GlyphI gl) {
    return gl.isVisible();
  }


  public void moveAbsolute(GlyphI glyph, double x, double y) {
    glyph.moveAbsolute(x, y);
  }

  public void moveAbsolute(List<GlyphI> glyphs, double x, double y) {
    for (GlyphI g : glyphs) {
      moveAbsolute(g, x, y);
    }
  }

  public void moveRelative(GlyphI glyph, double diffx, double diffy) {
    glyph.moveRelative(diffx, diffy);
  }

  public void moveRelative(List<GlyphI> glyphs, double x, double y) {
    for (GlyphI g : glyphs) {
      moveRelative(g, x, y);
    }
  }


  public void setScrollIncrementBehavior(TransformI.Dimension dim, int behavior) {
    scroll_behavior[dim.ordinal()] = behavior;
  }

  public int getScrollIncrementBehavior(TransformI.Dimension dim) {
    return scroll_behavior[dim.ordinal()];
  }


  /** Subclasses should implement this. Default does nothing.
   *  Implementations should add selections to the List 'selected',
   *  in addition to any other tasks specific to those classes.
   */
  public void select(GlyphI g) {
    // Implement in subclasses
  }

  public void select(List<GlyphI> glyphs) {
    if (glyphs == null) {
      return;
    }
    for (GlyphI g : glyphs) {
      select(g);
    }
  }

  /**
   *  Clears all selections by actaually calling {@link #deselect(GlyphI)}
   *  on each one as well as removing them from the List of selections.
   */
  public void clearSelected() {
    while (selected.size() > 0) {
      // selected.size() shrinks because deselect(glyph)
      //    calls selected.removeElement()
      Object gl = selected.get(0);
      if (gl == null) { selected.remove(0); } // xxx
      else {
        deselect((GlyphI)gl);
      }
    }
    selected.clear();
  }

  /** Subclasses should implement this. Default does nothing.
   *  Implementations should remove selections from the List 'selected',
   *  in addition to any other tasks specific to those classes.
   */
  public void deselect(GlyphI gl) {}

  public void deselect(List<GlyphI> glyphs) {
    // need to special case if vec argument is ref to same List as selected,
    //   since the deselect(Object) will cause shrinking of vec size as
    //   it is being looped through
    if (glyphs == null) {
      return;
    }
    if (glyphs == selected) {
      clearSelected();
    }
    for (GlyphI g : glyphs) {
      deselect(g);
    }
  }

  public List<GlyphI> getSelected() {
    return selected;
  }

  /** Clears all graphs from the widget.
   *  This default implementation simply removes all elements from the
   *  list of selections.  (It does this without calling clearSelected(),
   *  because it is faster to skip an explict call to deselect(GlyphI)
   *  for each Glyph.)
   *  Subclasses should call this method during their own implementation.
   *  Subclasses may choose to call clearSelected() before calling this
   *  method if they require an explicit call to deselect(GlyphI) for
   *  each Glyph.
   */
  public void clearWidget() {
    selected.clear();
    // reset glyph_hash
    glyph_hash = new Hashtable<GlyphI,Object>();

    // reset model_hash
    model_hash = new Hashtable<Object,Object>();

    models_have_multiple_glyphs = false;
  }


  // implementing MouseListener interface and collecting mouse events
  public void mouseClicked(MouseEvent e) { heardMouseEvent(e); }
  public void mouseEntered(MouseEvent e) { heardMouseEvent(e); }
  public void mouseExited(MouseEvent e) { heardMouseEvent(e); }
  public void mousePressed(MouseEvent e) { heardMouseEvent(e); }
  public void mouseReleased(MouseEvent e) { heardMouseEvent(e); }

  // implementing MouseMotionListener interface and collecting mouse events
  public void mouseDragged(MouseEvent e) { heardMouseEvent(e); }
  public void mouseMoved(MouseEvent e) { heardMouseEvent(e); }

  public void heardMouseEvent(MouseEvent evt) {
    // override in subclasses!
  }

  @Override
  public void addMouseListener(MouseListener l) {
    if (!mouse_listeners.contains(l)) {
      mouse_listeners.add(l);
    }
  }

  @Override
  public void removeMouseListener(MouseListener l) {
    mouse_listeners.remove(l);
  }

  @Override
  public void addMouseMotionListener(MouseMotionListener l) {
    if (!mouse_motion_listeners.contains(l)) {
      mouse_motion_listeners.add(l);
    }
  }

  @Override
  public void removeMouseMotionListener(MouseMotionListener l) {
    mouse_motion_listeners.remove(l);
  }

  @Override
  public void addKeyListener(KeyListener l) {
    if (!key_listeners.contains(l)) {
      key_listeners.add(l);
    }
  }

  @Override
  public void removeKeyListener(KeyListener l) {
    key_listeners.remove(l);
  }

  public void destroy() {
    key_listeners.clear();
    mouse_motion_listeners.clear();
    mouse_listeners.clear();
    glyph_hash.clear();
    model_hash.clear();
    selected.clear();
  }

    // Implementing KeyListener interface and collecting key events
    public void keyPressed(KeyEvent e) { heardKeyEvent(e); }
    public void keyReleased(KeyEvent e) { heardKeyEvent(e); }
    public void keyTyped(KeyEvent e) { heardKeyEvent(e); }

    public void heardKeyEvent(KeyEvent e) {
      int id = e.getID();
      if (! key_listeners.isEmpty()) {
        KeyEvent nevt = new KeyEvent(this, id, e.getWhen(), e.getModifiers(),
              e.getKeyCode(), e.getKeyChar());
        for (KeyListener kl : key_listeners) {
          if (id == KeyEvent.KEY_PRESSED) {
            kl.keyPressed(nevt);
          }
          else if (id == KeyEvent.KEY_RELEASED) {
            kl.keyReleased(nevt);
          }
          else if (id == KeyEvent.KEY_TYPED) {
            kl.keyTyped(nevt);
          }
        }
      }
    }

  /**
   *  Reshapes the component.
   *  Due to the way the Component class source code from Sun is written, it is this
   *  method that we must override, not setBounds(), even though this method
   *  is deprecated.
   *  <p>
   *  Users of this class should call setBounds(), but
   *  when extending this class, override this, not setBounds().
   *
   *  @deprecated use {@link #setBounds(int,int,int,int)}.
   */
  @Deprecated
  @Override
  public void reshape(int x, int y, int width, int height) {
    pref_widg_size.setSize(width, height);
    super.reshape(x, y, width, height);
  }

  @Override
  public Dimension getPreferredSize() {
    return pref_widg_size;
  }

  @Override
  public void setPreferredSize(Dimension d) {
    pref_widg_size = d;
  }

  @Override
  public void setCursor(Cursor cur) {
    Component comp[] = this.getComponents();
    for (int i=0; i<comp.length; i++) {
      comp[i].setCursor(cur);
    }
    super.setCursor(cur);
  }
}
