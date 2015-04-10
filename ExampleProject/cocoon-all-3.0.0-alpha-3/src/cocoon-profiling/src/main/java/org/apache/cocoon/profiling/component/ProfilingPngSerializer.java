/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.profiling.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.profiling.data.ProfilingData;
import org.apache.cocoon.profiling.data.ProfilingDataComparator;
import org.apache.cocoon.profiling.data.ProfilingDataHolder;

/**
 * A cocoon pipeline component that generates and serializes a png image that visualizes profiling
 * information.
 */
public class ProfilingPngSerializer implements Starter, Finisher, CachingPipelineComponent {

    private String id;

    private ProfilingData profilingData;

    private ProfilingDataHolder dataHolder;

    private OutputStream outputStream;

    /**
     * {@inheritDoc}
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return "image/png";
    }

    /**
     * Writes the image data to the output stream.
     */
    public void finish() {
    }

    /**
     * {@inheritDoc}
     */
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.id = (String) configuration.get("id");
        this.profilingData = this.dataHolder.get(this.id);
    }

    /**
     * {@inheritDoc}
     */
    public void execute() {
        if (this.profilingData == null) {
            new ErrorMessageDrawer().writeImage(this.outputStream);
        } else {
            new ProfilingDrawer(this.profilingData).writeImage(this.outputStream);
        }
    }

    public void setProfilingDataHolder(ProfilingDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    /**
     * {@inheritDoc}
     */
    public void setup(Map<String, Object> parameters) {
    }

    private class ProfilingDrawer {

        private static final int BAR_WIDTH = 520;

        private Map<String, Integer> lineNumberMap = new HashMap<String, Integer>();

        private List<List<ProfilingData>> lineNumber2profilingData = new ArrayList<List<ProfilingData>>();

        private long startTime;
        private long endTime;

        private final int LINE_HEIGHT = 20;
        private final int Y_OFFSET = 10;
        private final int X_OFFSET = 0;
        private final int MAX_STRING_WIDTH = 350;

        private int maxStringWidth = 0;

        private ProfilingData profilingData;

        public ProfilingDrawer(ProfilingData profilingData) {
            this.profilingData = profilingData;

            List<ProfilingData> data = this.transformTreeToList();

            this.startTime = data.get(0).getInvocationStartTime();
            this.endTime = data.get(0).getInvocationEndTime();

            for (ProfilingData pd : data) {
                if (this.getLineNumber(pd) == null) {
                    this.lineNumberMap.put(pd.getId(), this.lineNumberMap.size());
                }
            }

            for (ProfilingData pd : data) {
                Integer lineNumber = this.getLineNumber(pd);

                List<ProfilingData> pds;

                if (this.lineNumber2profilingData.size() <= lineNumber) {
                    pds = new ArrayList<ProfilingData>();
                    pds.add(pd);
                    this.lineNumber2profilingData.add(pds);
                } else {
                    this.lineNumber2profilingData.get(lineNumber).add(pd);
                }
            }
        }

        private List<ProfilingData> transformTreeToList() {
            ArrayList<ProfilingData> list = new ArrayList<ProfilingData>();
            this.traverse(this.profilingData, list);
            Collections.sort(list, new ProfilingDataComparator());

            return list;
        }

        private void traverse(ProfilingData profilingData2, ArrayList<ProfilingData> list) {
            list.add(profilingData2);

            for (ProfilingData pd : profilingData2.getChildren()) {
                this.traverse(pd, list);
            }
        }

        private float factor(long x) {
            long executionTime = this.endTime - this.startTime;
            long t = x - this.startTime;

            return t / (float) executionTime;
        }

        private Integer getLineNumber(ProfilingData pd) {
            return this.lineNumberMap.get(pd.getId());
        }

        private int getY(ProfilingData pd) {
            return this.getLineNumber(pd) * this.LINE_HEIGHT + this.Y_OFFSET;
        }

        public void writeImage(OutputStream os) {
            int height = this.X_OFFSET + this.lineNumber2profilingData.size() * this.LINE_HEIGHT;

            BufferedImage bufferedImage = new BufferedImage(1000, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setBackground(Color.WHITE);

            FontMetrics metrics = g.getFontMetrics();

            this.drawDisplayNameStrings(g, metrics);

            final int xOffset = this.X_OFFSET + this.maxStringWidth + 10;
            final int yOffset = -10;

            this.drawExecutionTimeStrings(g, xOffset);

            this.drawGrayBars(g, xOffset, yOffset);
            this.drawGreenBars(g, xOffset, yOffset);
            this.drawOrangeBars(g, xOffset, yOffset);
            this.drawCallerLines(g, xOffset, yOffset);

            g.dispose();

            try {
                ImageIO.write(bufferedImage, "png", os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void drawDisplayNameStrings(Graphics2D g, FontMetrics metrics) {
            g.setColor(Color.BLACK);
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                String displayName = pds.get(0).getDisplayName();
                for (ProfilingData pd : pds) {
                    if (pd.isDisplayNameSet()) {
                        displayName = pd.getDisplayName();
                        break;
                    }
                }

                displayName = this.adaptToMaxWidth(metrics, displayName);
                int stringWidth = metrics.stringWidth(displayName);

                g.drawString(displayName, this.X_OFFSET, this.getY(pds.get(0)));

                this.maxStringWidth = Math.max(this.maxStringWidth, stringWidth);
            }
        }

        private void drawGrayBars(Graphics2D g, final int xOffset, final int yOffset) {
            g.setColor(new Color(235, 235, 235));
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                g.fillRect(xOffset, this.getY(pds.get(0)) + yOffset, BAR_WIDTH, 15);
            }
        }

        private void drawGreenBars(Graphics2D g, final int xOffset, final int yOffset) {
            g.setColor(Color.GREEN.darker().darker());
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                int y = this.getY(pds.get(0)) + yOffset;
                for (ProfilingData pd : pds) {
                    int x = (int) (BAR_WIDTH * this.factor(pd.getInvocationStartTime()));
                    int width = (int) (BAR_WIDTH * this.factor(pd.getInvocationEndTime()) - x);

                    g.fillRect(xOffset + x, y, width, 15);
                }
            }
        }

        private void drawOrangeBars(Graphics2D g, final int xOffset, final int yOffset) {
            g.setColor(Color.ORANGE);
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                int y = this.getY(pds.get(0)) + yOffset;

                for (ProfilingData pd : pds) {
                    this.drawInactive(g, xOffset, y, pd.getChildren());
                }
            }
        }

        private void drawCallerLines(Graphics2D g, final int xOffset, final int yOffset) {
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                for (ProfilingData pd : pds) {
                    ProfilingData parent = pd.getParent();

                    if (parent == null) {
                        continue;
                    }

                    int thisLine = this.getY(pd) + yOffset + 7;
                    int parentLine = this.getY(parent) + yOffset + 7;

                    int startX = (int) (BAR_WIDTH * this.factor(pd.getInvocationStartTime()));
                    int endX = (int) (BAR_WIDTH * this.factor(pd.getInvocationEndTime()));

                    g.setColor(Color.DARK_GRAY);
                    g.drawLine(startX + xOffset, thisLine, startX + xOffset, parentLine);

                    g.setColor(Color.RED.darker());
                    g.drawLine(endX + xOffset, thisLine, endX + xOffset, parentLine);
                }
            }
        }

        private void drawExecutionTimeStrings(Graphics2D g, final int xOffset) {
            g.setColor(Color.BLACK);
            for (List<ProfilingData> pds : this.lineNumber2profilingData) {
                double executionTime = 0;
                for (ProfilingData pd : pds) {
                    executionTime += pd.getExecutionMillis();
                }

                String et = String.format(Locale.US, "%.3fms", executionTime);
                g.drawString(et, xOffset + BAR_WIDTH + 20, this.getY(pds.get(0)));
            }
        }

        private String adaptToMaxWidth(FontMetrics metrics, String displayName) {
            String s = displayName;
            while (metrics.stringWidth(s) > this.MAX_STRING_WIDTH) {
                int length = s.length();
                String start = s.substring(0, length / 2 - 1);
                String end = s.substring(length / 2 + 1, length);
                s = start + end;
            }

            if (!s.equals(displayName)) {
                int length = s.length();
                String start = s.substring(0, length / 2);
                String end = s.substring(length / 2, length);
                s = start + "..." + end;
            }

            return s;
        }

        private void drawInactive(Graphics g, int xOffset, int y, List<ProfilingData> children) {
            for (ProfilingData pd : children) {
                int x = (int) (BAR_WIDTH * this.factor(pd.getInvocationStartTime()));
                int width = (int) (BAR_WIDTH * this.factor(pd.getInvocationEndTime()) - x);

                g.fillRect(xOffset + x, y, width, 15);
            }
        }
    }

    private class ErrorMessageDrawer {

        public void writeImage(OutputStream os) {
            BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String errormsg = String.format("Profiling Data for id '%s' couldn't be found.",
                    ProfilingPngSerializer.this.id);

            FontMetrics metrics = g.getFontMetrics();
            int stringWidth = metrics.stringWidth(errormsg);

            bufferedImage = new BufferedImage(stringWidth + 2, 20, BufferedImage.TYPE_INT_ARGB);
            g = (Graphics2D) bufferedImage.getGraphics();
            g.setBackground(Color.WHITE);
            g.setColor(Color.BLACK);
            g.drawString(errormsg, 1, 15);
            g.dispose();

            try {
                ImageIO.write(bufferedImage, "png", os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public CacheKey constructCacheKey() {
        return new ParameterCacheKey("id", this.id);
    }
}
