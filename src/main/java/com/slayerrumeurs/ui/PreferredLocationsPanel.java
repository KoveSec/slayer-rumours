package com.slayerrumeurs.ui;

import com.slayerrumeurs.gear.InventorySetupService;
import com.slayerrumeurs.preference.PreferenceStore;
import com.slayerrumeurs.task.TaskCatalog;
import com.slayerrumeurs.task.TaskDefinition;
import com.slayerrumeurs.task.TaskLocation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class PreferredLocationsPanel extends PluginPanel
{
	private static final Color MUTED = new Color(170, 170, 170);
	private static final Color WARN = new Color(220, 150, 90);

	private final TaskCatalog catalog;
	private final PreferenceStore preferences;
	private final InventorySetupService inventorySetups;

	private final JPanel cards = new JPanel();
	private final JLabel resultCount = new JLabel();
	private final JTextField search = new JTextField();

	@Inject
	PreferredLocationsPanel(TaskCatalog catalog, PreferenceStore preferences, InventorySetupService inventorySetups)
	{
		this.catalog = catalog;
		this.preferences = preferences;
		this.inventorySetups = inventorySetups;

		setBorder(new EmptyBorder(8, 6, 8, 6));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(createHero());
		add(Box.createRigidArea(new Dimension(0, 8)));
		add(createSearch());
		add(Box.createRigidArea(new Dimension(0, 4)));

		resultCount.setFont(FontManager.getRunescapeSmallFont());
		resultCount.setForeground(MUTED);
		resultCount.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(resultCount);

		cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
		cards.setOpaque(false);
		cards.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(cards);

		search.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent e) { rebuild(); }
			@Override public void removeUpdate(DocumentEvent e) { rebuild(); }
			@Override public void changedUpdate(DocumentEvent e) { rebuild(); }
		});
		SwingUtilities.invokeLater(this::rebuild);
	}

	public void refreshSetupHints()
	{
		SwingUtilities.invokeLater(this::rebuild);
	}

	static BufferedImage createNavigationIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(160, 36, 36));
		g.fillOval(1, 1, 14, 14);
		g.setColor(new Color(230, 196, 80));
		g.drawOval(1, 1, 14, 14);
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(Font.BOLD, 9f));
		g.drawString("S", 5, 12);
		g.dispose();
		return image;
	}

	private JPanel createHero()
	{
		JPanel hero = new JPanel();
		hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
		hero.setBackground(new Color(40, 44, 48));
		hero.setBorder(new EmptyBorder(12, 10, 12, 10));
		hero.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel title = new JLabel("SLAYER RUMOURS");
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont());
		JLabel subtitle = new JLabel("<html><div style='width: 165px'>Pick a preferred location and Inventory Setup name for each Slayer task. "
			+ "Nav and Setup on the task strip use these mappings.</div></html>");
		subtitle.setForeground(MUTED);
		subtitle.setFont(FontManager.getRunescapeSmallFont());
		subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));
		hero.add(title);
		hero.add(subtitle);
		return hero;
	}

	private JPanel createSearch()
	{
		JPanel wrapper = new JPanel(new BorderLayout(6, 0));
		wrapper.setOpaque(false);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
		wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel label = new JLabel("Search");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(new Color(214, 163, 62));

		search.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		search.setForeground(Color.WHITE);
		search.setCaretColor(Color.WHITE);
		search.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR),
			new EmptyBorder(4, 6, 4, 6)));
		search.getAccessibleContext().setAccessibleName("Search tasks or locations");
		wrapper.add(label, BorderLayout.WEST);
		wrapper.add(search, BorderLayout.CENTER);
		return wrapper;
	}

	private void rebuild()
	{
		String query = search.getText().trim().toLowerCase(Locale.ENGLISH);
		cards.removeAll();
		int shown = 0;
		for (TaskDefinition task : catalog.getTasks())
		{
			if (!matches(task, query))
			{
				continue;
			}
			if (shown++ > 0)
			{
				cards.add(Box.createRigidArea(new Dimension(0, 6)));
			}
			cards.add(createTaskCard(task));
		}
		resultCount.setText(shown + (shown == 1 ? " task" : " tasks"));
		cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, cards.getPreferredSize().height));
		cards.revalidate();
		cards.repaint();
		revalidate();
	}

	private boolean matches(TaskDefinition task, String query)
	{
		if (query.isEmpty())
		{
			return true;
		}
		if (task.getDisplayName().toLowerCase(Locale.ENGLISH).contains(query)
			|| task.getId().toLowerCase(Locale.ENGLISH).contains(query))
		{
			return true;
		}
		for (String alias : task.getAliases())
		{
			if (alias.toLowerCase(Locale.ENGLISH).contains(query))
			{
				return true;
			}
		}
		for (TaskLocation location : task.getLocations())
		{
			if (location.getLabel() != null && location.getLabel().toLowerCase(Locale.ENGLISH).contains(query))
			{
				return true;
			}
		}
		String setup = preferences.getSetupName(task.getId());
		return setup != null && setup.toLowerCase(Locale.ENGLISH).contains(query);
	}

	private JPanel createTaskCard(TaskDefinition task)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(62, 63, 65)),
			new EmptyBorder(8, 8, 8, 8)));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel title = new JLabel(task.getDisplayName());
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.add(title);

		JLabel locationLabel = new JLabel("Location");
		locationLabel.setForeground(MUTED);
		locationLabel.setFont(FontManager.getRunescapeSmallFont());
		locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		locationLabel.setBorder(new EmptyBorder(6, 0, 2, 0));
		card.add(locationLabel);

		List<TaskLocation> locations = task.getLocations();
		JComboBox<TaskLocation> choice = new JComboBox<>(locations.toArray(new TaskLocation[0]));
		choice.setAlignmentX(Component.LEFT_ALIGNMENT);
		choice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		choice.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		choice.setForeground(Color.WHITE);
		choice.setFont(FontManager.getRunescapeSmallFont());
		TaskLocation selected = task.findLocation(preferences.getPreferredLocationId(task.getId()));
		if (selected == null)
		{
			selected = task.defaultLocation();
		}
		choice.setSelectedItem(selected);
		choice.addActionListener(event ->
		{
			TaskLocation location = (TaskLocation) choice.getSelectedItem();
			if (location != null)
			{
				preferences.setPreferredLocationId(task.getId(), location.getId());
			}
		});
		card.add(choice);

		JLabel setupLabel = new JLabel("Inventory Setup");
		setupLabel.setForeground(MUTED);
		setupLabel.setFont(FontManager.getRunescapeSmallFont());
		setupLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		setupLabel.setBorder(new EmptyBorder(6, 0, 2, 0));
		card.add(setupLabel);

		JTextField setupField = new JTextField();
		setupField.setAlignmentX(Component.LEFT_ALIGNMENT);
		setupField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		setupField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setupField.setForeground(Color.WHITE);
		setupField.setCaretColor(Color.WHITE);
		setupField.setFont(FontManager.getRunescapeSmallFont());
		String mapped = preferences.getSetupName(task.getId());
		if (mapped != null)
		{
			setupField.setText(mapped);
		}
		setupField.setToolTipText("Exact Inventory Setup name. Validated when Inventory Setups is installed.");

		JLabel hint = new JLabel(" ");
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);
		hint.setFont(FontManager.getRunescapeSmallFont());
		updateSetupHint(hint, setupField.getText());

		Runnable persist = () ->
		{
			preferences.setSetupName(task.getId(), setupField.getText());
			updateSetupHint(hint, setupField.getText());
		};
		setupField.addActionListener(event -> persist.run());
		setupField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent event)
			{
				persist.run();
			}
		});

		card.add(setupField);
		card.add(hint);
		return card;
	}

	private void updateSetupHint(JLabel hint, String value)
	{
		String name = value == null ? "" : value.trim();
		if (name.isEmpty())
		{
			hint.setText(" ");
			hint.setForeground(MUTED);
			return;
		}
		if (!inventorySetups.isAvailable())
		{
			hint.setText("Saved (Inventory Setups not detected)");
			hint.setForeground(MUTED);
			return;
		}
		if (inventorySetups.hasSetup(name))
		{
			hint.setText("Matches an Inventory Setup");
			hint.setForeground(new Color(120, 180, 120));
			return;
		}
		hint.setText("Unknown setup name");
		hint.setForeground(WARN);
	}
}
