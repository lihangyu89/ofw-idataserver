package org.eclipse.wb.swt;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Helper for sorting {@link TableViewer} by one of its {@link TableViewerColumn}s.
 * <p>
 * Originally from http://wiki.eclipse.org/index.php/JFaceSnippets, Snippet040TableViewerSorting.
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * @author Konstantin Scheglov <Konstantin.Scheglov@gmail.com>
 */
public class GridViewerColumnSorter extends ViewerComparator {
	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final GridViewerColumn m_column;
	private final GridTableViewer m_viewer;
	private int m_direction = NONE;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridViewerColumnSorter(GridViewerColumn column) {
		m_column = column;
		m_viewer = (GridTableViewer) column.getViewer();
		m_column.getColumn().setMoveable(true);// 排序必须
		m_column.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getComparator() != null) {
					if (m_viewer.getComparator() == GridViewerColumnSorter.this) {
						if (m_direction == ASC) {
							setSorter(DESC);
						} else if (m_direction == DESC) {
							setSorter(NONE);
						}
					} else {
						setSorter(ASC);
					}
				} else {
					setSorter(ASC);
				}
			}
		});
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public void setSorter(int direction) {
		if (direction == NONE) {
			m_column.getColumn().setSort(SWT.NONE);
			m_viewer.setComparator(null);
		} else {
			m_direction = direction;
			if (m_direction == ASC) {
				m_column.getColumn().setSort(SWT.DOWN);
			} else {
				m_column.getColumn().setSort(SWT.UP);
			}
			if (m_viewer.getComparator() == this) {
				m_viewer.refresh();
			} else {
				m_viewer.setComparator(this);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ViewerComparator
	//
	////////////////////////////////////////////////////////////////////////////
	public int compare(Viewer viewer, Object e1, Object e2) {
		return m_direction * doCompare(viewer, e1, e2);
	}
	/**
	 * Compares to elements of viewer. By default tries to compare values extracted from these elements using
	 * {@link #getValue(Object)}, because usually you want to compare value of some attribute.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int doCompare(Viewer viewer, Object e1, Object e2) {
		Object o1 = getValue(e1);
		Object o2 = getValue(e2);
		if (o1 instanceof Comparable && o2 instanceof Comparable) {
			return ((Comparable) o1).compareTo(o2);
		}
		return 0;
	}
	/**
	 * 
	 * @return the value to compare in {@link #doCompare(Viewer, Object, Object)}. Be default tries to get it
	 *         from {@link EditingSupport}. May return <code>null</code>.
	 */
	protected Object getValue(Object o) {
		try {
			EditingSupport editingSupport;
			{
				Method getEditingMethod = ViewerColumn.class.getDeclaredMethod("getEditingSupport",
					new Class[]{});
				getEditingMethod.setAccessible(true);
				editingSupport = (EditingSupport) getEditingMethod.invoke(m_column, new Object[]{});
			}
			if (editingSupport != null) {
				Method getValueMethod = EditingSupport.class.getDeclaredMethod("getValue",
					new Class[]{Object.class});
				getValueMethod.setAccessible(true);
				return getValueMethod.invoke(editingSupport, new Object[]{o});
			}
		} catch (Throwable e) {
		}
		return null;
	}
}