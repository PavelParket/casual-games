import { useState, useRef, useEffect, type HTMLAttributes } from "react";
import "../styles/combobox.css";
import { classNames } from "../../utils/classNames";

type ComboBoxOption = {
    value: string;
    label: string;
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

    const selectedOption = options.find((opt) => opt.value === value);
    const displayValue = selectedOption ? selectedOption.label : placeholder;

    const filteredOptions = isSearchable
        ? options.filter((opt) =>
            typeof opt.label === "string" &&
            opt.label.toLowerCase().includes(searchQuery.toLowerCase())
        )
        : options;

    const getTransparencyClass = () => {
        if (!transparency) {
            return "";
        }

        return `transparency-${transparency}`;
    };

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                selectRef.current &&
                !selectRef.current.contains(event.target as Node)
            ) {
                setIsOpen(false);
                setSearchQuery("");
            }
        };

        if (isOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
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
                    <span className="select-arrow">▼</span>
                </div>
            ) : (
                <div
                    className={classNames("select-trigger", disabled && "disabled", isOpen && "open")}
                    onClick={() => !disabled && setIsOpen(!isOpen)}
                >
                    <span
                        className={classNames("select-value", !selectedOption && "placeholder")}
                    >
                        {displayValue}
                    </span>
                    <span className="select-arrow">▼</span>
                </div>
            )}

            {isOpen && (
                <div className={classNames("select-dropdown", getTransparencyClass())}>
                    <div className="select-options">
                        {filteredOptions.length > 0 ? (
                            filteredOptions.map((option) => (
                                <div
                                    key={option.value}
                                    className={classNames("select-option", value === option.value && "selected")}
                                    onClick={() => handleSelect(option.value)}
                                >
                                    {option.label}
                                </div>
                            ))
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
