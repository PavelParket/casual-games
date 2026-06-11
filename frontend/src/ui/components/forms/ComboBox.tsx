import { useState, useRef, useEffect, type HTMLAttributes, type ReactNode } from "react";
import "../styles/combobox.css";
import { classNames } from "../../utils/classNames";
import { Icon } from "../common/Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";

type ComboBoxOption = {
    value: string;
    label: ReactNode | ((selected: boolean) => ReactNode);
    searchLabel?: string;
};

type Transparency = "easy" | "medium" | "hard";

type ComboBoxProps = HTMLAttributes<HTMLDivElement> & {
    options: ComboBoxOption[];
    value?: string;
    onValueChange: (value: string) => void;
    placeholder?: string;
    searchable?: boolean;
    disabled?: boolean;
    transparency?: Transparency;
};

export function ComboBox({
    options,
    value,
    onValueChange,
    placeholder = "Nothing chosen",
    searchable,
    disabled = false,
    transparency,
    className,
    ...rest
}: ComboBoxProps) {
    const isSearchable = searchable === true;
    const [isOpen, setIsOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const selectRef = useRef<HTMLDivElement>(null);

    const { getInverseIcon } = useThemedIcon();

    const selectedOption = options.find((opt) => opt.value === value);

    const renderLabel = (opt: ComboBoxOption, isSelected: boolean) => {
        if (typeof opt.label === "function") {
            return opt.label(isSelected);
        }
        return opt.label;
    };

    const displayValue = selectedOption ? renderLabel(selectedOption, false) : placeholder;

    const filteredOptions = isSearchable
        ? options.filter((opt) => {
            const text = opt.searchLabel || (typeof opt.label === "string" ? opt.label : "");
            return text.toLowerCase().includes(searchQuery.toLowerCase());
        })
        : options;

    const getTransparencyClass = () => {
        if (!transparency) return "";
        return `transparency-${transparency}`;
    };

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
                setIsOpen(false);
                setSearchQuery("");
            }
        };

        if (isOpen) document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [isOpen]);

    const handleSelect = (optionValue: string) => {
        onValueChange(optionValue);
        setIsOpen(false);
        setSearchQuery("");
    };

    return (
        <div className={classNames("select-wrapper", className)} ref={selectRef} {...rest}>
            {isOpen && isSearchable ? (
                <div className="select-search-trigger">
                    <input
                        type="text"
                        className="select-search-input-trigger"
                        placeholder="Search..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                        autoFocus
                    />
                    <Icon
                        src={getInverseIcon("expandMore")}
                        alt="arrow"
                        className="select-arrow"
                        size={16}
                        style={{ transform: "rotate(180deg)", transition: "transform 0.15s" }} />
                </div>
            ) : (
                <div
                    className={classNames("select-trigger", disabled && "disabled", isOpen && "open")}
                    onClick={() => !disabled && setIsOpen(!isOpen)}
                >
                    <span className={classNames("select-value", !selectedOption && "placeholder")}>
                        {displayValue}
                    </span>
                    <Icon src={getInverseIcon("expandMore")} alt="arrow" className="select-arrow" size={16} />
                </div>
            )}

            {isOpen && (
                <div className={classNames("select-dropdown", getTransparencyClass())}>
                    <div className="select-options">
                        {filteredOptions.length > 0 ? (
                            filteredOptions.map((option) => {
                                const isSelected = value === option.value;
                                return (
                                    <div
                                        key={option.value}
                                        className={classNames("select-option", isSelected && "selected")}
                                        onClick={() => handleSelect(option.value)}
                                    >
                                        {renderLabel(option, isSelected)}
                                    </div>
                                );
                            })
                        ) : (
                            <div className="select-option no-results">
                                No results found
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
