/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.whc.deid.shared.pojo.config.masking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ibm.whc.deid.shared.pojo.config.DeidMaskingConfig;
import com.ibm.whc.deid.shared.pojo.masking.MaskingProviderType;
import com.ibm.whc.deid.shared.util.InvalidMaskingConfigurationException;

@JsonInclude(Include.NON_NULL)
public class DateTimeConsistentShiftMaskingProviderConfig extends MaskingProviderConfig {

  private static final long serialVersionUID = 5266193758403530535L;

  /**
   * Determines where the shifted datetime value is ordered in relation to the original datetime
   * value.
   */
  public static enum DateShiftDirection {

    /**
     * The shifted value is before the original value.
     */
    before,

    /**
     * The shifted value can be before or after the original value.
     */
    beforeOrAfter,

    /**
     * The shifted value is after the original value.
     */
    after
  }

  public static final DateShiftDirection DEFAULT_DATE_SHIFT_DIRECTION =
      DateShiftDirection.beforeOrAfter;

  private ArrayList<String> customFormats = null;
  private int dateShiftMinimumDays = 1;
  private int dateShiftMaximumDays = 366;
  private DateShiftDirection dateShiftDirection = DEFAULT_DATE_SHIFT_DIRECTION;
  private String patientIdentifierPath = null;

  public DateTimeConsistentShiftMaskingProviderConfig() {
    type = MaskingProviderType.DATETIME_CONSISTENT_SHIFT;
  }

  public int getDateShiftMinimumDays() {
    return dateShiftMinimumDays;
  }

  public void setDateShiftMinimumDays(int dateShiftMinimumDays) {
    this.dateShiftMinimumDays = dateShiftMinimumDays;
  }

  public int getDateShiftMaximumDays() {
    return dateShiftMaximumDays;
  }

  public void setDateShiftMaximumDays(int dateShiftMaximumDays) {
    this.dateShiftMaximumDays = dateShiftMaximumDays;
  }

  public DateShiftDirection getDateShiftDirection() {
    return dateShiftDirection;
  }

  public void setDateShiftDirection(DateShiftDirection dateShiftDirection) {
    this.dateShiftDirection =
        dateShiftDirection == null ? DEFAULT_DATE_SHIFT_DIRECTION : dateShiftDirection;
  }

  public String getPatientIdentifierPath() {
    return patientIdentifierPath;
  }

  public void setPatientIdentifierPath(String patientIdentifierPath) {
    this.patientIdentifierPath = patientIdentifierPath;
  }

  public List<String> getCustomFormats() {
    return customFormats;
  }

  public void setCustomFormats(List<String> customFormats) {
    this.customFormats = customFormats == null ? null : new ArrayList<>(customFormats);
  }

  @Override
  public void validate(DeidMaskingConfig maskingConfig)
      throws InvalidMaskingConfigurationException {
    super.validate(maskingConfig);
    if (dateShiftMinimumDays < 0) {
      throw new InvalidMaskingConfigurationException(
          "`dateShiftMinimumDays` must be greater than or equal to 0");
    }
    if (dateShiftMaximumDays < dateShiftMinimumDays) {
      throw new InvalidMaskingConfigurationException(
          "`dateShiftMinimumDays` must be >= `dateShiftMinimumDays`");
    }
    if (patientIdentifierPath == null || patientIdentifierPath.trim().isEmpty()) {
      throw new InvalidMaskingConfigurationException("`patientIdentifierPath` is missing");
    }
    if (customFormats != null) {
      // TODO: complete validation
      // try {
      // new DateTimeFormatterBuilder().appendPattern(formatFixed);
      // } catch (IllegalArgumentException e) {
      // throw new InvalidMaskingConfigurationException(
      // "`formatFixed` is not valid: " + e.getMessage(), e);
      // }
    }
  }

  // TODO: verify List handled appropriately
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(customFormats, dateShiftDirection, dateShiftMaximumDays,
        dateShiftMinimumDays, patientIdentifierPath);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof DateTimeConsistentShiftMaskingProviderConfig)) {
      return false;
    }
    DateTimeConsistentShiftMaskingProviderConfig other =
        (DateTimeConsistentShiftMaskingProviderConfig) obj;
    return Objects.equals(customFormats, other.customFormats)
        && dateShiftDirection == other.dateShiftDirection
        && dateShiftMaximumDays == other.dateShiftMaximumDays
        && dateShiftMinimumDays == other.dateShiftMinimumDays
        && Objects.equals(patientIdentifierPath, other.patientIdentifierPath);
  }
}
